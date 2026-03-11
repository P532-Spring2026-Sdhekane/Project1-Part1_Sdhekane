let prevPrices = {};
let allNotifications = [];

function updateClock() {
  document.getElementById("clock").textContent =
    new Date().toLocaleTimeString();
}
setInterval(updateClock, 1000);
updateClock();

function fmt(n) {
  return (
    "$" +
    Number(n).toLocaleString("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  );
}

async function fetchPrices() {
  try {
    const r = await fetch("/api/prices");
    const prices = await r.json();
    renderPrices(prices);
    prevPrices = prices;
    const dot = document.getElementById("price-dot");
    dot.style.color = "#38bdf8";
    setTimeout(() => (dot.style.color = "#4ade80"), 300);
  } catch (e) {}
}

function renderPrices(prices) {
  const tbody = document.getElementById("price-body");
  tbody.innerHTML = "";
  for (const [ticker, price] of Object.entries(prices).sort()) {
    const prev = prevPrices[ticker] || price;
    const diff = price - prev;
    const pct = prev ? (diff / prev) * 100 : 0;
    const cls = diff >= 0 ? "up" : "down";
    const sign = diff >= 0 ? "+" : "";
    const tr = document.createElement("tr");
    tr.id = "price-row-" + ticker;
    tr.innerHTML = `<td style="color:#38bdf8;font-weight:600">${ticker}</td>
                    <td>${fmt(price)}</td>
                    <td class="${cls}">${sign}${pct.toFixed(2)}%</td>`;
    if (Math.abs(diff) > 0.001) {
      tr.classList.add(diff >= 0 ? "flash-up" : "flash-down");
    }
    tbody.appendChild(tr);
  }
}

async function fetchPortfolio() {
  try {
    const r = await fetch("/api/portfolio");
    const p = await r.json();
    document.getElementById("cash").textContent = fmt(p.cash);
    document.getElementById("total-value").textContent = fmt(p.totalValue);

    const tbody = document.getElementById("holdings-body");
    const prices = prevPrices;
    if (Object.keys(p.holdings).length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="4" class="empty">No holdings yet</td></tr>';
    } else {
      tbody.innerHTML = "";
      for (const [ticker, qty] of Object.entries(p.holdings)) {
        const price = prices[ticker] || 0;
        const val = price * qty;
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${ticker}</td><td>${qty}</td><td>${fmt(price)}</td><td>${fmt(val)}</td>`;
        tbody.appendChild(tr);
      }
    }
  } catch (e) {}
}

async function fetchPending() {
  try {
    const r = await fetch("/api/orders/pending");
    const orders = await r.json();
    const tbody = document.getElementById("pending-body");
    if (orders.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="4" class="empty">No pending orders</td></tr>';
    } else {
      tbody.innerHTML = "";
      orders.forEach((o) => {
        const tr = document.createElement("tr");
        const cls = o.side === "BUY" ? "buy" : "sell";
        tr.innerHTML = `<td style="color:#38bdf8">${o.ticker}</td>
                        <td><span class="badge ${cls}">${o.side}</span></td>
                        <td>${o.quantity}</td><td>${o.status}</td>`;
        tbody.appendChild(tr);
      });
    }
  } catch (e) {}
}

async function fetchHistory() {
  try {
    const r = await fetch("/api/trades");
    const trades = await r.json();
    const tbody = document.getElementById("history-body");
    if (trades.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="6" class="empty">No trades yet</td></tr>';
    } else {
      tbody.innerHTML = "";
      [...trades].reverse().forEach((t) => {
        const tr = document.createElement("tr");
        const cls = t.side === "BUY" ? "buy" : "sell";
        const ts = new Date(t.timestamp).toLocaleTimeString();
        tr.innerHTML = `<td>${ts}</td><td style="color:#38bdf8">${t.ticker}</td>
                        <td><span class="badge ${cls}">${t.side}</span></td>
                        <td>${t.quantity}</td><td>${fmt(t.price)}</td><td>${fmt(t.totalValue)}</td>`;
        tbody.appendChild(tr);
      });
    }
  } catch (e) {}
}

async function fetchNotifications() {
  try {
    const r = await fetch("/api/notifications");
    const data = await r.json();
    if (data.messages && data.messages.length > 0) {
      allNotifications = [...data.messages, ...allNotifications].slice(0, 30);
      const div = document.getElementById("notif-list");
      div.innerHTML = "";
      allNotifications.forEach((m) => {
        const p = document.createElement("p");
        p.className = "notif-item";
        p.textContent = m;
        div.appendChild(p);
      });
    }
  } catch (e) {}
}

function toggleLimitPrice() {
  const t = document.getElementById("order-type").value;
  document.getElementById("limit-price-row").style.display =
    t === "limit" ? "flex" : "none";
}

async function placeOrder(side) {
  const ticker = document.getElementById("order-ticker").value;
  const type = document.getElementById("order-type").value;
  const quantity = parseInt(document.getElementById("order-qty").value);
  const limitPrice = parseFloat(document.getElementById("limit-price").value);

  if (!quantity || quantity < 1) {
    showMsg("Quantity must be at least 1", false);
    return;
  }

  let url, body;
  if (type === "market") {
    url = "/api/orders/market";
    body = { ticker, side, quantity };
  } else {
    if (!limitPrice || limitPrice <= 0) {
      showMsg("Enter a valid limit price", false);
      return;
    }
    url = "/api/orders/limit";
    body = { ticker, side, quantity, limitPrice };
  }

  try {
    const r = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const data = await r.json();
    if (r.ok) {
      const msg =
        type === "market"
          ? `✓ ${side} ${quantity} ${ticker} executed`
          : `✓ Limit order placed for ${ticker}`;
      showMsg(msg, true);
      await Promise.all([
        fetchPortfolio(),
        fetchPending(),
        fetchHistory(),
        fetchNotifications(),
      ]);
    } else {
      showMsg(data.error || "Order failed", false);
    }
  } catch (e) {
    showMsg("Network error", false);
  }
}

function showMsg(msg, ok) {
  const el = document.getElementById("order-msg");
  el.textContent = msg;
  el.className = ok ? "msg-ok" : "msg-err";
  setTimeout(() => (el.textContent = ""), 4000);
}

async function pollAll() {
  await fetchPrices();
  await fetchPortfolio();
  await fetchPending();
  await fetchHistory();
  await fetchNotifications();
}

pollAll();
setInterval(pollAll, 5000);
