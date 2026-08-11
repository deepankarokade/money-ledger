const API_BASE = "/api";


// =========================================================
// HELPERS
// =========================================================

function getAccountId() {
    return document.getElementById("accountId").value;
}


function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        minimumFractionDigits: 2
    }).format(amount);
}


function formatDate(dateString) {

    const date = new Date(dateString);

    return date.toLocaleString("en-IN", {
        dateStyle: "medium",
        timeStyle: "short"
    });
}


function showToast(message, isError = false) {

    const toast = document.getElementById("toast");

    toast.textContent = message;

    toast.style.background =
        isError ? "#c43f3f" : "#17191c";

    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 3000);
}


// =========================================================
// LOAD ACCOUNT
// =========================================================

async function loadAccount() {

    const id = getAccountId();

    if (!id) {
        showToast("Enter an account ID", true);
        return;
    }

    try {

        const response = await fetch(
            `${API_BASE}/accounts/${id}/balance`
        );

        if (!response.ok) {

            const error = await response.json();

            throw new Error(
                error.error || "Account not found"
            );
        }

        const balance = await response.json();

        document.getElementById("balance").textContent =
            formatCurrency(balance);

        /*
         * Your current GET balance endpoint only returns
         * BigDecimal, so it does not return account holder
         * or account number.
         *
         * Therefore we leave these fields as placeholders
         * until you create a GET account endpoint.
         */

        document.getElementById("accountHolder").textContent =
            `Account #${id}`;

        document.getElementById("accountNumber").textContent =
            `ID ${id}`;

        await loadTransactions();

        showToast("Account loaded");

    } catch (error) {

        console.error(error);

        showToast(
            error.message || "Failed to load account",
            true
        );
    }
}


// =========================================================
// DEPOSIT
// =========================================================

async function deposit() {

    const id = Number(getAccountId());

    const amount =
        Number(document.getElementById("depositAmount").value);

    if (!amount || amount <= 0) {
        showToast(
            "Enter a valid deposit amount",
            true
        );

        return;
    }

    try {

        const response = await fetch(
            `${API_BASE}/accounts/deposit`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    id: id,
                    amount: amount
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {

            throw new Error(
                data.error || "Deposit failed"
            );
        }

        document.getElementById("depositAmount").value = "";

        updateBalanceFromResponse(data);

        await loadTransactions();

        showToast("Deposit successful");

    } catch (error) {

        console.error(error);

        showToast(
            error.message || "Deposit failed",
            true
        );
    }
}


// =========================================================
// WITHDRAW
// =========================================================

async function withdraw() {

    const id = Number(getAccountId());

    const amount =
        Number(document.getElementById("withdrawAmount").value);

    if (!amount || amount <= 0) {

        showToast(
            "Enter a valid withdrawal amount",
            true
        );

        return;
    }

    try {

        const response = await fetch(
            `${API_BASE}/accounts/withdraw`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    id: id,
                    amount: amount
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {

            throw new Error(
                data.error || "Withdrawal failed"
            );
        }

        document.getElementById("withdrawAmount").value = "";

        updateBalanceFromResponse(data);

        await loadTransactions();

        showToast("Withdrawal successful");

    } catch (error) {

        console.error(error);

        showToast(
            error.message || "Withdrawal failed",
            true
        );
    }
}


// =========================================================
// TRANSFER
// =========================================================

async function transfer() {

    const fromId = Number(getAccountId());

    const toId =
        Number(document.getElementById("toAccountId").value);

    const amount =
        Number(document.getElementById("transferAmount").value);


    if (!toId) {

        showToast(
            "Enter receiver account ID",
            true
        );

        return;
    }


    if (fromId === toId) {

        showToast(
            "Cannot transfer to the same account",
            true
        );

        return;
    }


    if (!amount || amount <= 0) {

        showToast(
            "Enter a valid transfer amount",
            true
        );

        return;
    }


    try {

        const response = await fetch(
            `${API_BASE}/accounts/transfer`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    fromId: fromId,
                    toId: toId,
                    amount: amount
                })
            }
        );


        if (!response.ok) {

            const data = await response.json();

            throw new Error(
                data.error || "Transfer failed"
            );
        }


        document.getElementById("toAccountId").value = "";

        document.getElementById("transferAmount").value = "";


        await loadAccount();

        showToast("Transfer successful");

    } catch (error) {

        console.error(error);

        showToast(
            error.message || "Transfer failed",
            true
        );
    }
}


// =========================================================
// UPDATE BALANCE
// =========================================================

function updateBalanceFromResponse(data) {

    if (data && data.balance !== undefined) {

        document.getElementById("balance").textContent =
            formatCurrency(data.balance);
    }
}


// =========================================================
// TRANSACTIONS
// =========================================================

async function loadTransactions() {

    const id = getAccountId();

    if (!id) {
        return;
    }

    try {

        const response = await fetch(
            `${API_BASE}/transactions/account/${id}`
        );

        if (!response.ok) {

            const data = await response.json();

            throw new Error(
                data.error || "Failed to load transactions"
            );
        }

        const transactions =
            await response.json();


        renderRecentTransactions(
            transactions.slice(-5).reverse()
        );

        renderAllTransactions(
            transactions
        );

    } catch (error) {

        console.error(error);

        document.getElementById(
            "recentTransactions"
        ).innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    Failed to load transactions
                </td>
            </tr>
        `;

        document.getElementById(
            "allTransactions"
        ).innerHTML = `
            <tr>
                <td colspan="6" class="empty">
                    Failed to load transactions
                </td>
            </tr>
        `;
    }
}


// =========================================================
// RENDER RECENT TRANSACTIONS
// =========================================================

function renderRecentTransactions(transactions) {

    const container =
        document.getElementById("recentTransactions");


    if (!transactions.length) {

        container.innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    No transactions yet
                </td>
            </tr>
        `;

        return;
    }


    container.innerHTML = transactions.map(transaction => {

        const type =
            transaction.type.toLowerCase();

        return `
            <tr>

                <td>
                    <span class="type ${type}">
                        ${transaction.type}
                    </span>
                </td>

                <td>
                    ${transaction.fromAccountId ?? "—"}
                </td>

                <td>
                    ${transaction.toAccountId ?? "—"}
                </td>

                <td class="amount">
                    ${formatCurrency(transaction.amount)}
                </td>

                <td>
                    ${formatDate(transaction.createdAt)}
                </td>

            </tr>
        `;

    }).join("");
}


// =========================================================
// RENDER ALL TRANSACTIONS
// =========================================================

function renderAllTransactions(transactions) {

    const container =
        document.getElementById("allTransactions");


    if (!transactions.length) {

        container.innerHTML = `
            <tr>
                <td colspan="6" class="empty">
                    No transactions yet
                </td>
            </tr>
        `;

        return;
    }


    container.innerHTML = transactions.map(transaction => {

        const type =
            transaction.type.toLowerCase();

        return `
            <tr>

                <td>
                    #${transaction.id}
                </td>

                <td>
                    <span class="type ${type}">
                        ${transaction.type}
                    </span>
                </td>

                <td>
                    ${transaction.fromAccountId ?? "—"}
                </td>

                <td>
                    ${transaction.toAccountId ?? "—"}
                </td>

                <td class="amount">
                    ${formatCurrency(transaction.amount)}
                </td>

                <td>
                    ${formatDate(transaction.createdAt)}
                </td>

            </tr>
        `;

    }).join("");
}


// =========================================================
// NAVIGATION
// =========================================================

function showSection(sectionName) {

    document.querySelectorAll(".section")
        .forEach(section => {
            section.classList.remove("active");
        });


    document.getElementById(sectionName)
        .classList.add("active");


    document.querySelectorAll(".nav-item")
        .forEach(button => {
            button.classList.remove("active");
        });


    if (sectionName === "dashboard") {

        document.querySelectorAll(".nav-item")[0]
            .classList.add("active");

    } else {

        document.querySelectorAll(".nav-item")[1]
            .classList.add("active");
    }
}


// =========================================================
// INITIAL LOAD
// =========================================================

document.addEventListener("DOMContentLoaded", () => {

    loadAccount();

});