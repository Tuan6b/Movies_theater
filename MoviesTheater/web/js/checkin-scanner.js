// QR scanner for the counter check-in page (UC46).
//
// Decoding runs through jsQR (js/jsqr.min.js), which is plain JavaScript and works on
// every browser. The browser's native BarcodeDetector is used instead when it happens
// to exist, because it is faster - but it must never be a requirement: Chromium only
// implements it where the OS provides a barcode API (Android, macOS, ChromeOS), so on
// Windows it is missing in Chrome, Edge and Brave alike.
//
// This file is deliberately pure ASCII. Every Vietnamese message lives in
// checkin.jsp as a data-msg-* attribute and is read from there. A .js file served
// without a charset - or served from a cache entry that was stored without one - gets
// decoded as Latin-1, and the Vietnamese text turned into mojibake on screen. Keeping
// the strings in the JSP removes that whole failure mode: the page declares UTF-8
// once, in its own content type. Only \u escapes for symbols are used below, which
// are ASCII in the source no matter how the file is decoded.
//
// getUserMedia still needs a secure context: https, or http on localhost. Over a LAN
// IP the browser blocks the camera, so that case is reported out loud.
(function () {
    var root = document.getElementById('cgvScan');
    if (!root) {
        return;
    }

    var base = root.dataset.base || '';
    var openBtn = document.getElementById('cgvScanOpen');
    var panel = document.getElementById('cgvScanPanel');
    var closeBtn = document.getElementById('cgvScanClose');
    var video = document.getElementById('cgvScanVideo');
    var statusEl = document.getElementById('cgvScanStatus');
    var logEl = document.getElementById('cgvScanLog');
    var hintEl = document.getElementById('cgvScanHint');

    var SCAN_INTERVAL_MS = 200;
    var SAME_CODE_COOLDOWN_MS = 4000;
    var FRAME_WIDTH = 640;

    // \u escapes, not the literal glyphs: the escape sequence is ASCII in the source,
    // so it survives being decoded as any charset.
    var MARK_OK = '\u2713 ';   // check mark
    var MARK_BAD = '\u2715 ';  // multiplication x
    var MIDDOT = ' \u00b7 ';   // middle dot separator

    var nativeDetector = null;
    var decoderName = '';
    var canvas = null;
    var ctx = null;
    var stream = null;
    var timer = null;
    var lastCode = '';
    var lastCodeAt = 0;
    var busy = false;
    var checkedInCount = 0;

    // Messages come from the JSP. The fallbacks are ASCII on purpose: they only show
    // if an attribute is missing, and they must stay readable even then.
    function msg(key, fallback) {
        var v = root.dataset['msg' + key];
        return (v && v.length > 0) ? v : fallback;
    }

    // Only the camera itself is a hard requirement. A missing BarcodeDetector is not
    // a reason to refuse - jsQR covers it.
    function blockingReason() {
        if (!window.isSecureContext) {
            return msg('Insecure', 'Camera requires https or http://localhost.');
        }
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return msg('Nocamera', 'This browser cannot access the camera.');
        }
        if (typeof window.jsQR !== 'function' && !('BarcodeDetector' in window)) {
            return msg('Nodecoder', 'QR decoder (js/jsqr.min.js) failed to load.');
        }
        return null;
    }

    function setStatus(text, kind) {
        statusEl.textContent = text;
        statusEl.className = 'cgv-scan-status' + (kind ? ' ' + kind : '');
    }

    // A short tone per result, so the employee does not have to watch the screen
    // while pointing the camera. Two pitches: accepted vs refused.
    function beep(ok) {
        try {
            var Ctx = window.AudioContext || window.webkitAudioContext;
            if (!Ctx) {
                return;
            }
            var actx = new Ctx();
            var osc = actx.createOscillator();
            var gain = actx.createGain();
            osc.frequency.value = ok ? 880 : 300;
            gain.gain.value = 0.08;
            osc.connect(gain);
            gain.connect(actx.destination);
            osc.start();
            setTimeout(function () {
                osc.stop();
                actx.close();
            }, ok ? 110 : 260);
        } catch (e) {
            // Audio is a nicety; never let it break scanning.
        }
    }

    function addLogRow(data) {
        var row = document.createElement('div');
        row.className = 'cgv-scan-row ' + (data.ok ? 'ok' : 'bad');

        var head = document.createElement('div');
        head.className = 'cgv-scan-row-head';
        head.textContent = (data.ok ? MARK_OK : MARK_BAD) + data.message;

        var meta = document.createElement('div');
        meta.className = 'cgv-scan-row-meta';
        var parts = [];
        if (data.code) { parts.push(data.code); }
        if (data.movie) { parts.push(data.movie); }
        if (data.seat) { parts.push(msg('Seat', 'Seat') + ' ' + data.seat); }
        if (data.customer) { parts.push(data.customer); }
        meta.textContent = parts.join(MIDDOT);

        row.appendChild(head);
        row.appendChild(meta);
        logEl.insertBefore(row, logEl.firstChild);
    }

    function submitCode(code) {
        busy = true;
        setStatus(msg('Checking', 'Checking') + ' ' + code, '');

        var body = new URLSearchParams();
        body.set('action', 'checkin_by_code');
        body.set('code', code);

        fetch(base + '/employee/checkin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            body: body.toString(),
            credentials: 'same-origin'
        }).then(function (res) {
            if (res.status === 401 || res.status === 403) {
                throw new Error(msg('Session', 'Session expired, reload the page.'));
            }
            return res.json();
        }).then(function (data) {
            addLogRow(data);
            beep(data.ok);
            if (data.ok) {
                checkedInCount++;
            }
            // data.message is JSON from the servlet, which declares UTF-8 itself.
            var tail = checkedInCount > 0
                ? MIDDOT + msg('Counted', 'checked in') + ' ' + checkedInCount
                : '';
            setStatus(data.message + tail, data.ok ? 'ok' : 'bad');
        }).catch(function (err) {
            setStatus(err.message || msg('Netfail', 'Request failed.'), 'bad');
            beep(false);
        }).then(function () {
            busy = false;
        });
    }

    function onDetected(raw) {
        var code = (raw || '').trim();
        if (!code) {
            return;
        }
        var now = Date.now();
        // The camera sees the same QR many times a second; act once per code until
        // the cooldown passes, otherwise one ticket would fire a burst of requests.
        if (code === lastCode && now - lastCodeAt < SAME_CODE_COOLDOWN_MS) {
            return;
        }
        lastCode = code;
        lastCodeAt = now;
        submitCode(code);
    }

    // Draws the current frame into an offscreen canvas and asks jsQR to read it.
    // Scaled down to FRAME_WIDTH: a QR held up to the lens survives it easily, and
    // getImageData over a full 1080p frame is too slow to run several times a second.
    function decodeWithJsQR() {
        var w = video.videoWidth;
        var h = video.videoHeight;
        if (!w || !h) {
            return null;
        }
        if (!canvas) {
            canvas = document.createElement('canvas');
            ctx = canvas.getContext('2d', { willReadFrequently: true });
        }
        var scale = Math.min(1, FRAME_WIDTH / w);
        var cw = Math.round(w * scale);
        var ch = Math.round(h * scale);
        if (canvas.width !== cw || canvas.height !== ch) {
            canvas.width = cw;
            canvas.height = ch;
        }
        ctx.drawImage(video, 0, 0, cw, ch);
        var frame = ctx.getImageData(0, 0, cw, ch);
        var result = window.jsQR(frame.data, frame.width, frame.height, {
            inversionAttempts: 'dontInvert'
        });
        return result ? result.data : null;
    }

    function detectFrame() {
        if (nativeDetector) {
            return nativeDetector.detect(video).then(function (codes) {
                return (codes && codes.length > 0) ? codes[0].rawValue : null;
            }).catch(function () {
                return null; // a single blurred frame is normal
            });
        }
        try {
            return Promise.resolve(decodeWithJsQR());
        } catch (e) {
            return Promise.resolve(null);
        }
    }

    function tick() {
        if (busy || video.readyState !== video.HAVE_ENOUGH_DATA) {
            return;
        }
        detectFrame().then(function (code) {
            if (code) {
                onDetected(code);
            }
        });
    }

    function pickDecoder() {
        if ('BarcodeDetector' in window) {
            try {
                nativeDetector = new window.BarcodeDetector({ formats: ['qr_code'] });
                decoderName = 'BarcodeDetector';
                return;
            } catch (e) {
                nativeDetector = null; // constructor can refuse the format
            }
        }
        if (typeof window.jsQR === 'function') {
            decoderName = 'jsQR';
        }
    }

    function start() {
        var reason = blockingReason();
        if (reason) {
            hintEl.textContent = reason;
            hintEl.hidden = false;
            return;
        }

        panel.hidden = false;
        openBtn.hidden = true;
        setStatus(msg('Starting', 'Starting camera...'), '');

        // facingMode is a hint: laptops only have a front camera and ignore it,
        // phones pick the back one, which is what you want at the door.
        navigator.mediaDevices.getUserMedia({
            video: { facingMode: 'environment', width: { ideal: 1280 } },
            audio: false
        }).then(function (s) {
            stream = s;
            video.srcObject = s;
            return video.play();
        }).then(function () {
            pickDecoder();
            // The decoder name is a developer detail, so it goes to the console
            // rather than into the line the employee reads.
            console.log('[checkin-scanner] decoder=' + decoderName);
            setStatus(msg('Ready', 'Hold the ticket QR in front of the camera.'), '');
            timer = setInterval(tick, SCAN_INTERVAL_MS);
        }).catch(function (err) {
            var text = msg('Camfail', 'Could not open the camera.');
            if (err && (err.name === 'NotAllowedError' || err.name === 'SecurityError')) {
                text = msg('Denied', 'Camera permission was denied.');
            } else if (err && err.name === 'NotFoundError') {
                text = msg('Notfound', 'No camera available on this machine.');
            } else if (err && err.name === 'NotReadableError') {
                text = msg('Busy', 'The camera is in use by another application.');
            }
            setStatus(text, 'bad');
        });
    }

    function stop() {
        if (timer) {
            clearInterval(timer);
            timer = null;
        }
        if (stream) {
            stream.getTracks().forEach(function (t) { t.stop(); });
            stream = null;
        }
        video.srcObject = null;
        nativeDetector = null;
        panel.hidden = true;
        openBtn.hidden = false;
        // The table behind the panel is now stale - reload so the new check-ins show.
        if (checkedInCount > 0) {
            window.location.reload();
        }
    }

    openBtn.addEventListener('click', start);
    closeBtn.addEventListener('click', stop);
    // Releasing the camera on navigation keeps the webcam light from staying on.
    window.addEventListener('pagehide', function () {
        if (timer) { clearInterval(timer); }
        if (stream) { stream.getTracks().forEach(function (t) { t.stop(); }); }
    });

    // One diagnostic line: says whether the browser refused, the decoder is missing,
    // or the page is not a secure context, without having to read the source.
    var reason = blockingReason();
    console.log('[checkin-scanner] secureContext=' + window.isSecureContext
        + ' | jsQR=' + (typeof window.jsQR)
        + ' | BarcodeDetector=' + (typeof window.BarcodeDetector)
        + ' | blocking=' + (reason || 'none'));

    // The button stays enabled even when something is wrong: an earlier version
    // disabled it, so clicking did nothing and no camera prompt ever appeared.
    if (reason) {
        openBtn.title = reason;
        hintEl.textContent = reason;
        hintEl.hidden = false;
    }
})();
