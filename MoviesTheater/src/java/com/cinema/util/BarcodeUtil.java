package com.cinema.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BarcodeUtil {

    public static BufferedImage generateBarcodeImage(String code, int width, int height)
            throws WriterException {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(code, BarcodeFormat.CODE_128, width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
        g.dispose();
        return image;
    }

    public static byte[] generateBarcodeBytes(String code, int width, int height)
            throws WriterException {
        BufferedImage image = generateBarcodeImage(code, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            writeMinimalPng(image, baos);
        }
        return baos.toByteArray();
    }

    public static String generateBarcodeDataUri(String code, int width, int height)
            throws WriterException {
        byte[] bytes = generateBarcodeBytes(code, width, height);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private static void writeMinimalPng(BufferedImage image, ByteArrayOutputStream baos) {
        try {
            int w = image.getWidth();
            int h = image.getHeight();
            int[] pixels = new int[w * h];
            image.getRGB(0, 0, w, h, pixels, 0, w);

            int stride = (w * 1 + 3) & ~3;
            byte[] rawData = new byte[stride * h + h];
            int offset = 0;
            for (int y = 0; y < h; y++) {
                rawData[offset++] = 0;
                for (int x = 0; x < w; x++) {
                    rawData[offset++] = (byte) ((pixels[y * w + x] & 0xFF) ^ 0xFF);
                }
                offset += stride - (w + 1);
            }

            baos.write(new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            });
            int crc;
            writeIntBE(baos, 13);
            baos.write("IHDR".getBytes("US-ASCII"));
            byte[] ihdrData = concat(
                intToBytes(w), intToBytes(h),
                new byte[]{ 8, 2, 0, 0, 0 }
            );
            baos.write(ihdrData);
            crc = crc32("IHDR".getBytes("US-ASCII"), ihdrData);
            writeIntBE(baos, crc);

            byte[] deflated;
            try {
                deflated = deflate(rawData);
            } catch (IOException ex) {
                return;
            }
            writeIntBE(baos, deflated.length);
            baos.write("IDAT".getBytes("US-ASCII"));
            crc = crc32("IDAT".getBytes("US-ASCII"), deflated);
            baos.write(deflated);
            writeIntBE(baos, crc);

            writeIntBE(baos, 0);
            baos.write("IEND".getBytes("US-ASCII"));
            crc = crc32("IEND".getBytes("US-ASCII"), new byte[0]);
            writeIntBE(baos, crc);
        } catch (Exception ignored) {
        }
    }

    private static void writeIntBE(ByteArrayOutputStream baos, int v) throws IOException {
        baos.write(new byte[]{
            (byte) (v >> 24), (byte) (v >> 16),
            (byte) (v >> 8),  (byte) v
        });
    }

    private static byte[] intToBytes(int v) {
        return new byte[]{
            (byte) (v >> 24), (byte) (v >> 16),
            (byte) (v >> 8),  (byte) v
        };
    }

    private static byte[] concat(byte[] a, byte[] b, byte[] c) {
        byte[] result = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length + b.length, c.length);
        return result;
    }

    private static byte[] deflate(byte[] data) throws IOException {
        java.util.zip.Deflater deflater = new java.util.zip.Deflater(
            java.util.zip.Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while (!deflater.finished()) {
            int len = deflater.deflate(buf);
            if (len > 0) baos.write(buf, 0, len);
        }
        deflater.end();
        return baos.toByteArray();
    }

    private static int crc32(byte[] type, byte[] data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(type);
        crc.update(data);
        return (int) crc.getValue();
    }
}
