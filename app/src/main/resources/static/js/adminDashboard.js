import { openModal } from "./components/modals.js";
import {
  getDoctors,
  filterDoctors,
  saveDoctor
} from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
  const addDoctorButton = document.getElementById("addDocBtn");
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (addDoctorButton) {
    addDoctorButton.addEventListener("click", () => {
      openModal("addDoctor");
    });
  }

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }

  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }

  if (filterSpecialty) {
    filterSpecialty.addEventListener(
      "change",
      filterDoctorsOnChange
    );
  }

  loadDoctorCards();
});

async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
  }
}

async function filterDoctorsOnChange() {
  const searchBar = document
    .getElementById("searchBar")
    .value.trim();

  const filterTime =
    document.getElementById("filterTime").value;

  const filterSpecialty =
    document.getElementById("filterSpecialty").value;

  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty =
    filterSpecialty.length > 0 ? filterSpecialty : null;

  try {
    const response = await filterDoctors(
      name,
      time,
      specialty
    );

    const doctors = response.doctors || [];
    const contentDiv = document.getElementById("content");

    contentDiv.innerHTML = "";

    if (doctors.length > 0) {
      doctors.forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    } else {
      contentDiv.innerHTML =
        "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    console.error("Failed to filter doctors:", error);
    alert("An error occurred while filtering doctors.");
  }
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

window.adminAddDoctor = async function () {
  const name = document
    .getElementById("doctorName")
    .value.trim();

  const specialty =
    document.getElementById("specialization").value;

  const email = document
    .getElementById("doctorEmail")
    .value.trim();

  const password =
    document.getElementById("doctorPassword").value;

  const phone = document
    .getElementById("doctorPhone")
    .value.trim();

  const availableTimes = Array.from(
    document.querySelectorAll(
      'input[name="availability"]:checked'
    )
  ).map((checkbox) => checkbox.value);

  const token = localStorage.getItem("token");

  if (!token) {
    alert("Session expired or invalid login.");
    window.location.href = "/";
    return;
  }

  const doctor = {
    name,
    specialty,
    email,
    password,
    phone,
    availableTimes
  };

  const result = await saveDoctor(doctor, token);

  if (result.success) {
    alert(result.message || "Doctor added successfully.");

    document.getElementById("modal").style.display = "none";

    await loadDoctorCards();
  } else {
    alert(result.message || "Failed to add doctor.");
  }
};