# SOFTWARE DESIGN SPECIFICATION
**Project Name (Code): Movies_theater**
– Hanoi, July 2026 –

## Table of Contents
I. Record of Changes
II. Software Design Document
1. High Level Design
    1.1 Software Architecture
    1.2 Package Diagram
    1.3 Database Design
2. State Transition Diagrams
    2.1 User Authentication State
    2.2 Ticket Booking Process State
3. Detailed Design
    3.1 Class Diagrams (Domain Model)
    3.2 Authentication & User Management (Sequence)
    3.3 Ticket Booking & Payment (Sequence)
    3.4 Movie & Schedule Management (Sequence)

---

## I. Record of Changes

| Date | A*M, D | In charge | Change Description |
|---|---|---|---|
| 13/07/2026 | A | AI Assistant | Initialized the SDS document based on SRS |
| 13/07/2026 | M | AI Assistant | Expanded to cover full Database Schema (20 tables) and detailed Use Cases |

*\*A - Added, M - Modified, D - Deleted*

---

## II. Software Design Document

### 1. High Level Design

#### 1.1 Software Architecture
The Movies Theater system utilizes a classic **MVC (Model-View-Controller)** architecture built with Java technologies (Servlets and JSP).

```mermaid
graph TD
    Client[Web Browser / Client]
    
    subgraph Presentation Layer
        JSP[JSP Views]
        Controllers[Servlets / Controllers]
    end
    
    subgraph Business Layer
        DAO[Data Access Objects - DAO]
        Models[Java Beans / Models]
    end
    
    Database[(SQL Server DB)]
    
    Client -- HTTP Requests --> Controllers
    Controllers -- Forward Data --> JSP
    JSP -- HTML/UI --> Client
    
    Controllers -- Use/Update --> Models
    Controllers -- Call DB Operations --> DAO
    DAO -- Query/Execute --> Database
    Database -- Return Result Set --> DAO
```

#### 1.2 Package Diagram

```mermaid
classDiagram
    class com_cinema_controller {
        <<Servlet>>
        +LoginController
        +BookingController
        +MovieController
        +AdminController
    }
    class com_cinema_dao {
        <<Data Access>>
        +AccountDAO
        +TicketDAO
        +MovieDAO
        +ScheduleDAO
    }
    class com_cinema_model {
        <<Entities>>
        +Account
        +Ticket
        +Movie
        +Room
    }
    class com_cinema_util {
        <<Utilities>>
        +DBContext
        +HashUtil
    }
    class com_cinema_filter {
        <<Filter>>
        +AuthFilter
        +EncodingFilter
    }

    com_cinema_controller --> com_cinema_dao : uses
    com_cinema_controller --> com_cinema_model : creates/uses
    com_cinema_dao --> com_cinema_model : maps to
    com_cinema_dao --> com_cinema_util : connects via
    com_cinema_filter --> com_cinema_controller : intercepts
```

#### 1.3 Database Design
The system's database, `CinemaBookingDB`, comprises 20 interconnected tables supporting user management, movie metadata, theater layout, scheduling, and transactions.

**(1) Role & Account Management**
- `Role`: (RoleID PK, RoleName). Defines Admin, Customer, Employee.
- `Account`: (AccountID PK, Email, Password, RoleID FK, IsBlocked). Core credentials.
- `UserProfile`: (AccountID PK/FK, FullName, PhoneNumber, DoB, Address, AvatarURL).
- `UnlockRequest`: Logs requests from blocked users to be unblocked by Admin.

**(2) Movie Information**
- `Movie`: (MovieID PK, MovieName, Duration, ReleaseDate, Poster, Trailer, IsActive...).
- `Genre`: (GenreID PK, GenreName).
- `MovieGenre`: Junction table (MovieID FK, GenreID FK).
- `MovieReview`: (ReviewID PK, MovieID FK, AccountID FK, Rating, Comment).

**(3) Cinema Layout & Scheduling**
- `Room`: (RoomID PK, RoomNumber, RoomType, Capacity, IsActive).
- `Seat`: (SeatID PK, RoomID FK, RowChar, ColNumber, SeatType).
- `Schedule`: (ScheduleID PK, MovieID FK, RoomID FK, ShowDate, StartTime, EndTime, Price).

**(4) Booking & Transactions**
- `Ticket`: (TicketID PK, InvoiceID FK, ScheduleID FK, SeatID FK, Price, Status).
- `Invoice`: (InvoiceID PK, AccountID FK, TotalAmount, PromotionID FK, PaymentMethod, PaymentStatus).
- `Food`: (FoodID PK, FoodName, Price, Image, IsActive).
- `InvoiceFood`: Junction mapping food purchased in an invoice.
- `Promotion`: (PromotionID PK, Code, DiscountAmount, ValidFrom, ValidTo).

**(5) Employee Management & Logging**
- `WorkShift`: Employee shift tracking.
- `ShiftExchangeRequest`: Shift hand-offs raised by an employee and approved or declined by a Manager.
- `SystemConfig` & `SystemLog`: App configurations and audit logs.

---

### 2. State Transition Diagrams

#### 2.1 Ticket Booking State (State Transition Diagram)

```mermaid
stateDiagram-v2
    [*] --> BrowseMovies : User visits site
    BrowseMovies --> SelectShowtime : Click on a Movie
    
    SelectShowtime --> SelectSeats : Choose Date & Time
    SelectSeats --> SelectFood : Seats Selected & Locked
    SelectFood --> Checkout : Proceed to Pay
    
    Checkout --> PaymentSuccess : Valid Payment
    Checkout --> PaymentFailed : Invalid Payment / Timeout
    
    PaymentSuccess --> [*] : Ticket Issued & Invoice Generated
    PaymentFailed --> SelectSeats : Seats Released, Retry allowed
```

---

### 3. Detailed Design

#### 3.1 Domain Model Class Diagram
```mermaid
classDiagram
    class Account {
        +int AccountID
        +String Email
        +String Password
        +int RoleID
    }
    class Movie {
        +int MovieID
        +String MovieName
        +int Duration
        +boolean IsActive
    }
    class Schedule {
        +int ScheduleID
        +Date ShowDate
        +Time StartTime
        +double Price
    }
    class Ticket {
        +int TicketID
        +String Status
        +double Price
    }
    class Invoice {
        +int InvoiceID
        +double TotalAmount
        +String PaymentStatus
    }
    
    Account "1" -- "0..*" Invoice : makes
    Invoice "1" -- "1..*" Ticket : contains
    Schedule "1" -- "0..*" Ticket : sold for
    Movie "1" -- "0..*" Schedule : scheduled as
```


#### 3.2 Authentication & User Management Sequence (UC01 - UC02)

**Sequence Diagram for Login Process**
```mermaid
sequenceDiagram
    actor User
    participant Browser as JSP Page
    participant Ctrl as LoginController
    participant DAO as AccountDAO
    participant DB as Database
    
    User->>Browser: Enter Email & Password, click Login
    Browser->>Ctrl: POST /login (email, password)
    Ctrl->>Ctrl: Validate input format
    Ctrl->>DAO: authenticate(email, password)
    DAO->>DB: SELECT * FROM Account WHERE Email=?
    DB-->>DAO: Return ResultSet
    DAO->>DAO: Check Password Hash & IsBlocked status
    
    alt Credentials Valid & Not Blocked
        DAO-->>Ctrl: Return Account object
        Ctrl->>Browser: Set Session & Redirect to Home/Dashboard
        Browser-->>User: Display Home Page
    else Invalid Credentials or Blocked
        DAO-->>Ctrl: Return null
        Ctrl->>Browser: Forward to Login page with Error Msg
        Browser-->>User: Display Error
    end
```

#### 3.3 Ticket Booking & Payment Sequence

**Sequence Diagram for Ticket Booking**
```mermaid
sequenceDiagram
    actor User
    participant View as JSP/UI
    participant Ctrl as BookingController
    participant InvoiceDAO as InvoiceDAO
    participant TicketDAO as TicketDAO
    participant DB as SQL Server
    
    User->>View: Select Seats & Confirm Payment
    View->>Ctrl: POST /bookTicket (ScheduleID, SeatIDs, PaymentInfo)
    Ctrl->>Ctrl: Calculate TotalAmount
    
    alt Payment Gateway Success
        Ctrl->>InvoiceDAO: createInvoice(AccountID, TotalAmount)
        InvoiceDAO->>DB: INSERT INTO Invoice
        DB-->>InvoiceDAO: InvoiceID
        
        loop For each SeatID
            Ctrl->>TicketDAO: createTicket(InvoiceID, ScheduleID, SeatID)
            TicketDAO->>DB: INSERT INTO Ticket
        end
        
        Ctrl->>View: Redirect to E-Ticket Page
        View-->>User: Show E-Ticket
    else Payment Failed
        Ctrl->>View: Redirect back with Payment Error
        View-->>User: Display Error
    end
```

#### 3.4 Movie Management (Admin) Sequence

**Sequence Diagram for Adding a Movie**
```mermaid
sequenceDiagram
    actor Admin
    participant View as AdminJSP
    participant Ctrl as MovieController
    participant DAO as MovieDAO
    participant DB as SQL Server
    
    Admin->>View: Fill Movie Form (Name, Duration, Image)
    View->>Ctrl: POST /admin/addMovie
    Ctrl->>Ctrl: Upload Image & Generate URL
    Ctrl->>DAO: insertMovie(Movie obj)
    DAO->>DB: INSERT INTO Movie (...)
    DB-->>DAO: Success/MovieID
    DAO-->>Ctrl: true
    Ctrl->>View: Redirect to Movie List
    View-->>Admin: Show updated list
```
