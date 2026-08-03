// header.js

function renderHeader() {
    const headerDiv = document.getElementById("header");
  
    if (!headerDiv) {
      return;
    }
  
    if (window.location.pathname.endsWith("/")) {
      localStorage.removeItem("userRole");
      localStorage.removeItem("token");
  
      headerDiv.innerHTML = `
        <header class="header">
          <div class="logo-section">
            <img
              src="/assets/images/logo/logo.png"
              alt="Hospital CMS Logo"
              class="logo-img"
            >
            <span class="logo-title">Hospital CMS</span>
          </div>
        </header>
      `;
  
      return;
    }
  
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");
  
    if (
      (role === "loggedPatient" ||
        role === "admin" ||
        role === "doctor") &&
      !token
    ) {
      localStorage.removeItem("userRole");
      localStorage.removeItem("token");
  
      alert("Session expired or invalid login. Please log in again.");
      window.location.href = "/";
      return;
    }
  
    let headerContent = `
      <header class="header">
        <div class="logo-section">
          <img
            src="/assets/images/logo/logo.png"
            alt="Hospital CMS Logo"
            class="logo-img"
          >
          <span class="logo-title">Hospital CMS</span>
        </div>
  
        <nav>
    `;
  
    if (role === "admin") {
      headerContent += `
        <button id="addDocBtn" class="adminBtn">
          Add Doctor
        </button>
        <a href="#" id="logoutBtn">Logout</a>
      `;
    } else if (role === "doctor") {
      headerContent += `
        <button id="doctorHomeBtn" class="adminBtn">
          Home
        </button>
        <a href="#" id="logoutBtn">Logout</a>
      `;
    } else if (role === "patient") {
      headerContent += `
        <button id="patientLogin" class="adminBtn">
          Login
        </button>
        <button id="patientSignup" class="adminBtn">
          Sign Up
        </button>
      `;
    } else if (role === "loggedPatient") {
      headerContent += `
        <button id="home" class="adminBtn">
          Home
        </button>
        <button id="patientAppointments" class="adminBtn">
          Appointments
        </button>
        <a href="#" id="logoutPatientBtn">Logout</a>
      `;
    }
  
    headerContent += `
        </nav>
      </header>
    `;
  
    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
  }
  
  function attachHeaderButtonListeners() {
    const addDoctorButton = document.getElementById("addDocBtn");
    const patientLoginButton = document.getElementById("patientLogin");
    const patientSignupButton = document.getElementById("patientSignup");
    const doctorHomeButton = document.getElementById("doctorHomeBtn");
    const homeButton = document.getElementById("home");
    const appointmentsButton = document.getElementById(
      "patientAppointments"
    );
    const logoutButton = document.getElementById("logoutBtn");
    const logoutPatientButton = document.getElementById(
      "logoutPatientBtn"
    );
  
    if (addDoctorButton) {
      addDoctorButton.addEventListener("click", async () => {
        const { openModal } = await import("./modals.js");
        openModal("addDoctor");
      });
    }
  
    if (patientLoginButton) {
      patientLoginButton.addEventListener("click", async () => {
        const { openModal } = await import("./modals.js");
        openModal("patientLogin");
      });
    }
  
    if (patientSignupButton) {
      patientSignupButton.addEventListener("click", async () => {
        const { openModal } = await import("./modals.js");
        openModal("patientSignup");
      });
    }
  
    if (doctorHomeButton) {
      doctorHomeButton.addEventListener("click", () => {
        selectRole("doctor");
      });
    }
  
    if (homeButton) {
      homeButton.addEventListener("click", () => {
        window.location.href =
          "/pages/loggedPatientDashboard.html";
      });
    }
  
    if (appointmentsButton) {
      appointmentsButton.addEventListener("click", () => {
        window.location.href =
          "/pages/patientAppointments.html";
      });
    }
  
    if (logoutButton) {
      logoutButton.addEventListener("click", (event) => {
        event.preventDefault();
        logout();
      });
    }
  
    if (logoutPatientButton) {
      logoutPatientButton.addEventListener(
        "click",
        (event) => {
          event.preventDefault();
          logoutPatient();
        }
      );
    }
  }
  
  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
  }
  
  function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
  }
  
  renderHeader();