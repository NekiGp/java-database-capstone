# Smart Clinic Management System Database Design

## MySQL Database Design

### Table: patients
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(20), Not Null
- address: VARCHAR(255), Not Null

### Table: doctors
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- specialty: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(20), Not Null

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null

### Design Notes
- One doctor can have many appointments.
- One patient can have many appointments.
- Email addresses should be unique.
- Appointment conflicts should be prevented in application logic.
- Patient appointment history should be retained.

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```

### Design Notes
- Prescriptions are stored in MongoDB because they support flexible document structures.
- The document references the appointment using appointmentId.
- Doctor notes and pharmacy information can evolve without changing a relational schema.
