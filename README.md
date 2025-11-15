# 🅱️ BPARK – Parking Management System  
**Final Software Engineering Project – ORT Braude College**

BPARK is a complete end-to-end parking management system implemented in **Java, JavaFX, OCSF Client–Server architecture, and MySQL**.  
The system supports customers, attendants, and managers, providing a full workflow for **reservations**, **active parking**, **payment calculation**, and **site activity monitoring**.

---

## 🚗 Features

### 👤 Customer
- Register and log in as a subscriber  
- Create parking reservations  
- Enter parking lot using a generated parking code  
- Automatic payment based on duration and delays  
- View current and future reservations  

### 🅿️ Parking Operations
- Track **active parkings** in real time  
- Late exit calculations  
- Extension handling  
- Parking history & duration reports  

### 🧑‍💼 Management
- Employee login (attendant / manager roles)  
- Management dashboard  
- Site Activity Report (Active Parkings + Future Reservations)  
- Parking Duration Report  
- Subscriber details viewer  
- Manual override operations  

### 🖥️ Terminal Kiosk
- Parking-code entry screen  
- “Forgot code” flow  
- Drop-off and pick-up screens  
- Designed for physical on-site terminals  

---

## 🛠️ Architecture

### Technologies
- Java 17  
- JavaFX  
- OCSF  
- MySQL  
- JDBC  
- MVC pattern  


### Project Structure
BparkClientSide/     → JavaFX Client App  
BparkServerSide/     → OCSF Server + DB Logic  
common/              → Shared classes (Requests/Responses/Entities)  
doc/                 → Generated JavaDoc  



## 🗄️ Database
Main tables include:
- subscribers  
- reservations  
- active_parkings  
- parking_history  
- employees  

All database operations are handled via `mysqlConnection.java` using the `DBExecutor` abstraction.

---

## ▶️ How to Run

1. Clone the repository  
2. Import into IntelliJ (with JavaFX SDK configured)  
3. Import the MySQL schema  
4. Run the `EchoServer` (server side)  
5. Run the JavaFX client  
6. Log in as customer or employee and use the system  

---

## 👥 Contributors

- **Dean Baranes** — Client UI, Terminal interface, Management module  
- **Carmel Peretz** — Server logic, MySQL integration, OCSF communication  


### Project Structure
