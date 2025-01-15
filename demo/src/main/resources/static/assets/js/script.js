const API_BASE_URL = "http://localhost:8082/api";

document
  .getElementById("startIndexing")
  .addEventListener("click", async function () {
    try {
      const response = await fetch(`${API_BASE_URL}/startIndexing`, {
        method: "POST", // Используем POST вместо GET
      });
      console.log(response);
      if (!response.ok) {
        throw new Error("Failed to start indexing.");
      }

      alert("Indexing started successfully!");
    } catch (error) {
      console.error("Error:", error);
      alert("An error occurred while starting indexing.");
    }
  });

// Stop indexing handler
document
  .getElementById("stopIndexing")
  .addEventListener("click", async function () {
    try {
      const response = await fetch(`${API_BASE_URL}/stopIndexing`, {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Failed to stop indexing.");
      }

      alert("Indexing stopped successfully!");
    } catch (error) {
      console.error("Error:", error);
      alert("An error occurred while stopping indexing.");
    }
  });

// Handler for Index Site button
document
  .getElementById("indexSiteButton")
  .addEventListener("click", async function () {
    const siteUrl = document.getElementById("siteUrlInput").value.trim();
    const siteName = "Custom Site"; // Или добавьте поле ввода для имени сайта, если нужно

    if (!siteUrl) {
      alert("Please enter a valid site URL.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/index`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ url: siteUrl, name: siteName }),
      });

      if (!response.ok) {
        throw new Error("Failed to index the site.");
      }

      alert("Site indexed successfully!");
    } catch (error) {
      console.error("Error:", error);
      alert("An error occurred while indexing the site.");
    }
  });

async function loadSites() {
  try {
    const response = await fetch(`${API_BASE_URL}/sites`);
    if (!response.ok) {
      throw new Error("Ошибка при загрузке списка сайтов");
    }

    const sites = await response.json();
    console.log("Полученные сайты:", sites); // Логируем для отладки

    const siteSelect = document.getElementById("siteSelect");
    if (!siteSelect) {
      console.error("Элемент siteSelect не найден");
      return;
    }

    // Очистка select перед добавлением
    siteSelect.innerHTML = '<option value="">Все сайты</option>';

    if (Array.isArray(sites) && sites.length > 0) {
      sites.forEach((site) => {
        const option = document.createElement("option");
        option.value = site.url;
        option.textContent = site.name || "Без названия";
        siteSelect.appendChild(option);
      });
    } else {
      console.warn("Массив сайтов пуст");
    }
  } catch (error) {
    console.error("Ошибка загрузки сайтов:", error);
    alert("Не удалось загрузить список сайтов.");
  }
}

// Вызов функции после загрузки DOM
document.addEventListener("DOMContentLoaded", loadSites);

// Обработчик формы поиска
document
  .getElementById("searchForm")
  .addEventListener("submit", async function (event) {
    event.preventDefault();

    const query = document.getElementById("searchInput").value.trim();
    const siteUrl = document.getElementById("siteSelect").value; // Получение выбранного сайта

    if (!query) {
      alert("Введите поисковый запрос.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/search`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ query, site: siteUrl }),
      });

      if (!response.ok) {
        throw new Error("Не удалось выполнить поиск.");
      }

      const data = await response.json();
      displayResults(data);
    } catch (error) {
      console.error("Ошибка выполнения поиска:", error);
      alert("Произошла ошибка при выполнении поиска.");
    }
  });

// Загрузка списка сайтов при загрузке страницы
document.addEventListener("DOMContentLoaded", loadSites);

function displayResults(data) {
  const resultsDiv = document.getElementById("results");
  const resultsList = document.getElementById("resultsList");

  // Очищаем список результатов
  resultsList.innerHTML = "";

  if (data && data.length > 0) {
    data.forEach((result) => {
      const relevance = document.createElement("p");
      const resultDiv = document.createElement("div");
      resultDiv.classList.add("result");

      // Создаём заголовок с ссылкой
      const titleLink = document.createElement("a");
      titleLink.href = result.url;
      titleLink.target = "_blank";
      titleLink.textContent = result.title || result.url;
      titleLink.style.fontWeight = "bold";
      titleLink.style.fontSize = "18px";

      // Убираем HTML-теги из сниппета
      const cleanSnippet = result.snippet.replace(/<\/?[^>]+(>|$)/g, "").trim();

      // Создаём сниппет
      const snippet = document.createElement("p");
      snippet.textContent = cleanSnippet || "Описание отсутствует";
      snippet.style.marginTop = "5px";
      relevance.textContent = `Релевантность: ${result.relevance.toFixed(2)}`;
      // Добавляем заголовок и сниппет в результат
      resultDiv.appendChild(titleLink);
      resultDiv.appendChild(snippet);
      resultDiv.appendChild(relevance);
      resultsList.appendChild(resultDiv);
    });
  } else {
    resultsList.innerHTML = "<p>Результаты не найдены.</p>";
  }

  // Отображаем блок результатов
  resultsDiv.style.display = "block";
}

document
  .getElementById("addSiteButton")
  .addEventListener("click", async function () {
    const url = document.getElementById("siteUrl").value.trim();
    const name = document.getElementById("siteName").value.trim();

    if (!url || !name) {
      alert("Введите корректные данные для URL и имени сайта.");
      return;
    }

    try {
      const response = await fetch("/api/addSite", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ url, name }),
      });

      const result = await response.text();
      if (response.ok) {
        alert("Сайт успешно добавлен!");
      } else {
        alert(`Ошибка: ${result}`);
      }
    } catch (error) {
      console.error("Ошибка при добавлении сайта:", error);
      alert("Произошла ошибка при добавлении сайта.");
    }
  });

document.addEventListener("DOMContentLoaded", () => {
  fetch("/api/statistics")
    .then((response) => response.json())
    .then((data) => {
      console.log(data);
      document.getElementById("total-pages").textContent = data.totalPages;
      document.getElementById("total-lemmas").textContent = data.totalLemmas;

      const siteSelect = document.getElementById("site-select");
      data.siteStats.forEach((site) => {
        const option = document.createElement("option");
        option.value = site.url;
        option.textContent = site.name;
        siteSelect.appendChild(option);
      });

      siteSelect.addEventListener("change", () => {
        const selectedSite = data.siteStats.find(
          (site) => site.url === siteSelect.value
        );
        document.getElementById("site-url").textContent = selectedSite.url;
        document.getElementById("site-name").textContent = selectedSite.name;
        document.getElementById("site-pages").textContent =
          selectedSite.pagesCount;
        document.getElementById("site-lemmas").textContent =
          selectedSite.lemmasCount;
      });
    });
});
