import { showBookingOverlay } from "../loggedPatient.js";
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.textContent = `Specialty: ${doctor.specialty}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  const availability = document.createElement("p");
  availability.textContent = `Availability: ${
    Array.isArray(doctor.availableTimes) &&
    doctor.availableTimes.length > 0
      ? doctor.availableTimes.join(", ")
      : "Not available"
  }`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";

    removeBtn.addEventListener("click", async () => {
      const confirmation = confirm(
        `Are you sure you want to delete ${doctor.name}?`
      );

      if (!confirmation) {
        return;
      }

      const token = localStorage.getItem("token");

      if (!token) {
        alert("Session expired or invalid login.");
        window.location.href = "/";
        return;
      }

      const result = await deleteDoctor(doctor.id, token);

      alert(result.message);

      if (result.success) {
        card.remove();
      }
    });

    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";

    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });

    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";

    bookNow.addEventListener("click", async (event) => {
      const token = localStorage.getItem("token");

      if (!token) {
        localStorage.setItem("userRole", "patient");
        alert("Patient needs to login first.");
        window.location.href = "/pages/patientDashboard.html";
        return;
      }

      const patientData = await getPatientData(token);

      if (!patientData) {
        alert("Unable to retrieve patient information.");
        return;
      }

      showBookingOverlay(event, doctor, patientData);
    });

    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}