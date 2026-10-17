// Shop E-Commerce Fullstack Single Page Application (SPA)
// API-First Integration with Spring Boot 3.4+ Backend

const API_BASE = '/api/v1';

// App State
const state = {
    currentTab: 'storefront',
    categories: [],
    products: [],
    customers: [],
    currentCustomer: null,
    cart: JSON.parse(localStorage.getItem('shop_cart') || '[]'),
    filters: {
        categoryId: '',
        q: '',
        minPrice: '',
        maxPrice: '',
        sort: 'createdAt,desc',
        page: 0,
        size: 12
    },
    pagination: {
        page: 0,
        totalPages: 1,
        totalElements: 0
    },
    adminOrders: [],
    adminFilterStatus: ''
};

// Formatting Helper
const formatVND = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Toast Notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            ${type === 'success' 
                ? '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>' 
                : '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>'}
        </svg>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Initial Loading
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    setupEventListeners();
    await loadCategories();
    await loadCustomers();
    await loadProducts();
    updateCartUI();
}

function setupEventListeners() {
    // Mode Switcher
    document.querySelectorAll('.switch-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.switch-tab').forEach(t => t.classList.remove('active'));
            e.target.classList.add('active');
            state.currentTab = e.target.dataset.tab;
            toggleView(state.currentTab);
        });
    });

    // Search and Sort
    const searchInput = document.getElementById('searchInput');
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            state.filters.q = e.target.value;
            state.filters.page = 0;
            loadProducts();
        }, 300);
    });

    document.getElementById('sortSelect').addEventListener('change', (e) => {
        state.filters.sort = e.target.value;
        state.filters.page = 0;
        loadProducts();
    });

    // Modals
    document.getElementById('cartBtn').addEventListener('click', openCartModal);
    document.getElementById('ordersBtn').addEventListener('click', openCustomerOrdersModal);
    document.getElementById('customerSelect').addEventListener('change', (e) => {
        state.currentCustomer = state.customers.find(c => c.id === e.target.value) || null;
    });
}

function toggleView(tabName) {
    const storefront = document.getElementById('storefrontView');
    const admin = document.getElementById('adminView');

    if (tabName === 'storefront') {
        storefront.style.display = 'block';
        admin.style.display = 'none';
        loadProducts();
    } else {
        storefront.style.display = 'none';
        admin.style.display = 'block';
        loadAdminData();
    }
}

// ===================== API CALLS & DATA FETCHING =====================

async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        state.categories = await res.json();
        renderCategoryChips();
        renderCategoryDropdown();
    } catch (err) {
        console.error('Lỗi tải danh mục:', err);
    }
}

async function loadCustomers() {
    try {
        const res = await fetch(`${API_BASE}/customers?size=50`);
        const json = await res.json();
        state.customers = json.data || [];
        if (state.customers.length > 0 && !state.currentCustomer) {
            state.currentCustomer = state.customers[0];
        }
        renderCustomerSelect();
    } catch (err) {
        console.error('Lỗi tải khách hàng:', err);
    }
}

async function loadProducts() {
    const grid = document.getElementById('productsGrid');
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">Đang tải sản phẩm...</div>';

    try {
        const params = new URLSearchParams({
            page: state.filters.page,
            size: state.filters.size,
            sort: state.filters.sort
        });
        if (state.filters.categoryId) params.append('categoryId', state.filters.categoryId);
        if (state.filters.q) params.append('q', state.filters.q);
        if (state.filters.minPrice) params.append('minPrice', state.filters.minPrice);
        if (state.filters.maxPrice) params.append('maxPrice', state.filters.maxPrice);

        const res = await fetch(`${API_BASE}/products?${params.toString()}`);
        const json = await res.json();
        state.products = json.data || [];
        state.pagination = json.pagination || { page: 0, totalPages: 1, totalElements: 0 };

        renderProducts();
    } catch (err) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--danger);">Không thể kết nối máy chủ API</div>';
    }
}

// ===================== UI RENDERING =====================

function renderCategoryChips() {
    const container = document.getElementById('categoryChips');
    container.innerHTML = `
        <button class="chip ${state.filters.categoryId === '' ? 'active' : ''}" onclick="selectCategory('')">
            Tất cả sản phẩm
        </button>
    `;
    state.categories.forEach(cat => {
        container.innerHTML += `
            <button class="chip ${state.filters.categoryId === cat.id ? 'active' : ''}" onclick="selectCategory('${cat.id}')">
                ${cat.name}
            </button>
        `;
    });
}

function renderCategoryDropdown() {
    const select = document.getElementById('prodCategory');
    if (!select) return;
    select.innerHTML = '<option value="">Chọn danh mục...</option>';
    state.categories.forEach(cat => {
        select.innerHTML += `<option value="${cat.id}">${cat.name}</option>`;
    });
}

function renderCustomerSelect() {
    const select = document.getElementById('customerSelect');
    select.innerHTML = '';
    state.customers.forEach(cust => {
        const selected = (state.currentCustomer && state.currentCustomer.id === cust.id) ? 'selected' : '';
        select.innerHTML += `<option value="${cust.id}" ${selected}>${cust.fullName} (${cust.email})</option>`;
    });
}

function selectCategory(id) {
    state.filters.categoryId = id;
    state.filters.page = 0;
    renderCategoryChips();
    loadProducts();
}

function renderProducts() {
    const grid = document.getElementById('productsGrid');
    if (state.products.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 60px; color: var(--text-muted);">Không tìm thấy sản phẩm phù hợp.</div>';
        return;
    }

    grid.innerHTML = state.products.map(p => {
        const stockBadge = p.quantity > 10 
            ? `<span class="badge-stock badge-in-stock">Còn ${p.quantity} sp</span>`
            : (p.quantity > 0 
                ? `<span class="badge-stock badge-low-stock">Sắp hết: ${p.quantity} sp</span>`
                : `<span class="badge-stock badge-out-of-stock">Hết hàng</span>`);

        return `
            <div class="product-card" id="card-${p.id}">
                <div class="card-image-wrap">
                    <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=500'}" alt="${p.name}" class="card-image">
                    ${stockBadge}
                </div>
                <div class="card-body">
                    <div class="card-category">${p.category ? p.category.name : 'Thiết bị'}</div>
                    <h3 class="card-title">${p.name}</h3>
                    <p class="card-desc">${p.description || 'Sản phẩm công nghệ chính hãng'}</p>
                    <div class="card-footer">
                        <div class="card-price">${formatVND(p.price)}</div>
                        <button class="btn btn-primary" onclick="addToCart('${p.id}')" ${p.quantity === 0 ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                                <line x1="3" y1="6" x2="21" y2="6"></line>
                                <path d="M16 10a4 4 0 0 1-8 0"></path>
                            </svg>
                            ${p.quantity === 0 ? 'Hết hàng' : 'Thêm giỏ'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// ===================== CART & CHECKOUT OPERATIONS =====================

function addToCart(productId) {
    const product = state.products.find(p => p.id === productId);
    if (!product) return;

    if (product.quantity <= 0) {
        showToast('Sản phẩm đã hết hàng trong kho!', 'error');
        return;
    }

    const existing = state.cart.find(item => item.productId === productId);
    if (existing) {
        if (existing.quantity >= product.quantity) {
            showToast(`Bạn đã chọn tối đa ${product.quantity} sản phẩm có trong kho!`, 'error');
            return;
        }
        existing.quantity += 1;
    } else {
        state.cart.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            imageUrl: product.imageUrl,
            quantity: 1
        });
    }

    saveCart();
    updateCartUI();
    showToast(`Đã thêm "${product.name}" vào giỏ hàng`);
}

function updateCartQuantity(productId, delta) {
    const item = state.cart.find(i => i.productId === productId);
    if (!item) return;

    const product = state.products.find(p => p.id === productId);
    const newQty = item.quantity + delta;

    if (product && newQty > product.quantity) {
        showToast(`Tồn kho chỉ còn ${product.quantity} chiếc!`, 'error');
        return;
    }

    if (newQty <= 0) {
        state.cart = state.cart.filter(i => i.productId !== productId);
    } else {
        item.quantity = newQty;
    }

    saveCart();
    updateCartUI();
    renderCartModalItems();
}

function removeFromCart(productId) {
    state.cart = state.cart.filter(i => i.productId !== productId);
    saveCart();
    updateCartUI();
    renderCartModalItems();
}

function saveCart() {
    localStorage.setItem('shop_cart', JSON.stringify(state.cart));
}

function updateCartUI() {
    const totalItems = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cartBadge').textContent = totalItems;
}

function openCartModal() {
    renderCartModalItems();
    document.getElementById('cartModal').classList.add('active');
}

function renderCartModalItems() {
    const container = document.getElementById('cartItemsList');
    const totalEl = document.getElementById('cartTotal');

    if (state.cart.length === 0) {
        container.innerHTML = '<div style="text-align: center; padding: 30px; color: var(--text-muted);">Giỏ hàng đang trống</div>';
        totalEl.textContent = formatVND(0);
        document.getElementById('checkoutBtn').disabled = true;
        return;
    }

    document.getElementById('checkoutBtn').disabled = false;
    let total = 0;

    container.innerHTML = state.cart.map(item => {
        const subtotal = item.price * item.quantity;
        total += subtotal;
        return `
            <div class="cart-item">
                <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=500'}" class="cart-item-img">
                <div class="cart-item-info">
                    <div style="font-weight: 600;">${item.name}</div>
                    <div style="font-size: 0.85rem; color: var(--text-muted);">${formatVND(item.price)}</div>
                </div>
                <div class="cart-qty-controls">
                    <button class="qty-btn" onclick="updateCartQuantity('${item.productId}', -1)">-</button>
                    <span style="font-weight: 700; min-width: 20px; text-align: center;">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateCartQuantity('${item.productId}', 1)">+</button>
                </div>
                <div style="font-weight: 700; color: #818CF8; min-width: 90px; text-align: right;">${formatVND(subtotal)}</div>
                <button class="close-btn" style="font-size: 1.1rem; color: #EF4444;" onclick="removeFromCart('${item.productId}')">&times;</button>
            </div>
        `;
    }).join('');

    totalEl.textContent = formatVND(total);
}

async function proceedCheckout() {
    if (state.cart.length === 0) return;
    if (!state.currentCustomer) {
        showToast('Vui lòng chọn thông tin khách hàng!', 'error');
        return;
    }

    const checkoutBtn = document.getElementById('checkoutBtn');
    checkoutBtn.disabled = true;
    checkoutBtn.textContent = 'Đang xử lý giao dịch...';

    const orderPayload = {
        customerId: state.currentCustomer.id,
        items: state.cart.map(i => ({
            productId: i.productId,
            quantity: i.quantity
        }))
    };

    try {
        const res = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderPayload)
        });

        const data = await res.json();

        if (res.status === 201) {
            showToast(`Đặt hàng thành công! Mã đơn: ${data.id.substring(0, 8)}... (Tồn kho đã trừ)`, 'success');
            state.cart = [];
            saveCart();
            updateCartUI();
            closeModal('cartModal');
            // Cập nhật lại sản phẩm để thấy số lượng tồn kho đã giảm tức thì
            await loadProducts();
        } else {
            showToast(`Đặt hàng thất bại: ${data.message || 'Lỗi không xác định'}`, 'error');
        }
    } catch (err) {
        showToast('Không thể kết nối đến máy chủ đặt hàng', 'error');
    } finally {
        checkoutBtn.disabled = false;
        checkoutBtn.textContent = 'Xác nhận Đặt hàng (Checkout)';
    }
}

// ===================== CUSTOMER ORDERS & CANCELLATION =====================

async function openCustomerOrdersModal() {
    if (!state.currentCustomer) {
        showToast('Vui lòng chọn khách hàng trước', 'error');
        return;
    }

    const container = document.getElementById('customerOrdersList');
    container.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-muted);">Đang tải lịch sử đơn hàng...</div>';
    document.getElementById('ordersModal').classList.add('active');

    try {
        const res = await fetch(`${API_BASE}/customers/${state.currentCustomer.id}/orders`);
        const json = await res.json();
        const orders = json.data || [];

        if (orders.length === 0) {
            container.innerHTML = '<div style="text-align: center; padding: 30px; color: var(--text-muted);">Khách hàng chưa có đơn hàng nào</div>';
            return;
        }

        container.innerHTML = orders.map(ord => {
            const canCancel = ord.status === 'PENDING';
            const itemsHtml = ord.items.map(it => `
                <div style="display: flex; justify-content: space-between; font-size: 0.85rem; color: var(--text-secondary); margin: 4px 0;">
                    <span>• ${it.productName} (x${it.quantity})</span>
                    <span>${formatVND(it.subtotal)}</span>
                </div>
            `).join('');

            return `
                <div class="order-card">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-weight: 700;">Đơn #${ord.id.substring(0, 8)}</span>
                        <span class="status-pill status-${ord.status}">${ord.status}</span>
                    </div>
                    <div style="margin-bottom: 8px;">${itemsHtml}</div>
                    <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-glass); padding-top: 8px; margin-top: 8px;">
                        <span style="font-weight: 700; color: #818CF8;">Tổng: ${formatVND(ord.totalAmount)}</span>
                        ${canCancel ? `
                            <button class="btn btn-danger" style="padding: 6px 12px; font-size: 0.8rem;" onclick="cancelCustomerOrder('${ord.id}')">
                                Hủy đơn & Hoàn kho
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        container.innerHTML = '<div style="color: var(--danger); text-align: center;">Không thể tải lịch sử đơn hàng</div>';
    }
}

async function cancelCustomerOrder(orderId) {
    if (!confirm('Bạn có chắc muốn hủy đơn hàng này? Hệ thống sẽ tự động hoàn trả số lượng vào kho.')) return;

    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/cancel`, { method: 'POST' });
        const json = await res.json();

        if (res.ok) {
            showToast('Hủy đơn thành công! Tồn kho đã được tự động hoàn trả vào CSDL.', 'success');
            await openCustomerOrdersModal();
            await loadProducts();
        } else {
            showToast(`Hủy đơn thất bại: ${json.message}`, 'error');
        }
    } catch (err) {
        showToast('Lỗi khi hủy đơn hàng', 'error');
    }
}

// ===================== ADMIN DASHBOARD OPERATIONS =====================

async function loadAdminData() {
    await loadAdminOrders();
    await renderAdminProductsTable();
    updateAdminKPIs();
}

async function loadAdminOrders() {
    try {
        const url = state.adminFilterStatus 
            ? `${API_BASE}/orders?status=${state.adminFilterStatus}&size=50`
            : `${API_BASE}/orders?size=50`;
        const res = await fetch(url);
        const json = await res.json();
        state.adminOrders = json.data || [];
        renderAdminOrdersTable();
    } catch (err) {
        console.error('Lỗi tải đơn hàng Admin:', err);
    }
}

function updateAdminKPIs() {
    const totalRev = state.adminOrders
        .filter(o => o.status === 'PAID' || o.status === 'SHIPPED')
        .reduce((sum, o) => sum + o.totalAmount, 0);
    const totalOrders = state.adminOrders.length;
    const pendingOrders = state.adminOrders.filter(o => o.status === 'PENDING').length;
    const totalStock = state.products.reduce((sum, p) => sum + p.quantity, 0);

    document.getElementById('kpiRevenue').textContent = formatVND(totalRev);
    document.getElementById('kpiOrders').textContent = totalOrders;
    document.getElementById('kpiPending').textContent = pendingOrders;
    document.getElementById('kpiStock').textContent = totalStock;
}

function renderAdminOrdersTable() {
    const tbody = document.getElementById('adminOrdersBody');
    if (state.adminOrders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 30px;">Chưa có đơn hàng nào</td></tr>';
        return;
    }

    tbody.innerHTML = state.adminOrders.map(ord => {
        let actionButtons = '';
        if (ord.status === 'PENDING') {
            actionButtons = `
                <button class="btn btn-primary" style="padding: 4px 8px; font-size: 0.78rem;" onclick="adminUpdateStatus('${ord.id}', 'PAID')">Duyệt PAID</button>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 0.78rem;" onclick="adminCancelOrder('${ord.id}')">Hủy & Hoàn kho</button>
            `;
        } else if (ord.status === 'PAID') {
            actionButtons = `
                <button class="btn btn-accent" style="padding: 4px 8px; font-size: 0.78rem;" onclick="adminUpdateStatus('${ord.id}', 'SHIPPED')">Giao SHIPPED</button>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 0.78rem;" onclick="adminCancelOrder('${ord.id}')">Hủy</button>
            `;
        } else {
            actionButtons = `<span style="font-size: 0.8rem; color: var(--text-muted);">${ord.status === 'SHIPPED' ? 'Đã hoàn tất' : 'Đã hủy'}</span>`;
        }

        return `
            <tr>
                <td style="font-family: monospace;">#${ord.id.substring(0, 8)}</td>
                <td>${ord.customerName || 'N/A'}</td>
                <td>${ord.items ? ord.items.length : 0} món</td>
                <td style="font-weight: 700; color: #818CF8;">${formatVND(ord.totalAmount)}</td>
                <td><span class="status-pill status-${ord.status}">${ord.status}</span></td>
                <td><div style="display: flex; gap: 6px;">${actionButtons}</div></td>
            </tr>
        `;
    }).join('');
}

function renderAdminProductsTable() {
    const tbody = document.getElementById('adminProductsBody');
    tbody.innerHTML = state.products.map(p => `
        <tr>
            <td>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <img src="${p.imageUrl || 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=500'}" style="width: 36px; height: 36px; border-radius: 4px; object-fit: cover;">
                    <span style="font-weight: 600;">${p.name}</span>
                </div>
            </td>
            <td>${p.category ? p.category.name : ''}</td>
            <td style="font-weight: 700; color: #818CF8;">${formatVND(p.price)}</td>
            <td>
                <div style="display: flex; align-items: center; gap: 6px;">
                    <span style="font-weight: 700; min-width: 25px;">${p.quantity}</span>
                    <button class="qty-btn" onclick="quickUpdateStock('${p.id}', 5)">+5</button>
                    <button class="qty-btn" onclick="quickUpdateStock('${p.id}', 10)">+10</button>
                </div>
            </td>
            <td>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 0.78rem;" onclick="deleteProduct('${p.id}')">Xóa</button>
            </td>
        </tr>
    `).join('');
}

async function quickUpdateStock(productId, addAmount) {
    const p = state.products.find(x => x.id === productId);
    if (!p) return;

    try {
        const res = await fetch(`${API_BASE}/products/${productId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: p.quantity + addAmount })
        });
        if (res.ok) {
            showToast(`Cập nhật tồn kho "${p.name}" lên ${p.quantity + addAmount}`, 'success');
            await loadProducts();
            renderAdminProductsTable();
            updateAdminKPIs();
        }
    } catch (err) {
        showToast('Lỗi cập nhật tồn kho', 'error');
    }
}

async function adminUpdateStatus(orderId, status) {
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            showToast(`Đã chuyển đơn sang trạng thái ${status}`, 'success');
            await loadAdminOrders();
            updateAdminKPIs();
        } else {
            const err = await res.json();
            showToast(err.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi cập nhật trạng thái đơn', 'error');
    }
}

async function adminCancelOrder(orderId) {
    if (!confirm('Xác nhận hủy đơn hàng này và hoàn kho?')) return;
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/cancel`, { method: 'POST' });
        if (res.ok) {
            showToast('Hủy đơn thành công & tồn kho đã tự động cộng hoàn lại!', 'success');
            await loadAdminOrders();
            await loadProducts();
            renderAdminProductsTable();
            updateAdminKPIs();
        }
    } catch (err) {
        showToast('Lỗi khi hủy đơn hàng', 'error');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    try {
        const res = await fetch(`${API_BASE}/products/${productId}`, { method: 'DELETE' });
        if (res.status === 204) {
            showToast('Đã xóa sản phẩm thành công', 'success');
            await loadProducts();
            renderAdminProductsTable();
        }
    } catch (err) {
        showToast('Không thể xóa sản phẩm', 'error');
    }
}

function openAddProductModal() {
    renderCategoryDropdown();
    document.getElementById('addProductModal').classList.add('active');
}

async function submitCreateProduct(event) {
    event.preventDefault();
    const payload = {
        name: document.getElementById('prodName').value,
        categoryId: document.getElementById('prodCategory').value,
        price: parseFloat(document.getElementById('prodPrice').value),
        quantity: parseInt(document.getElementById('prodQty').value, 10),
        description: document.getElementById('prodDesc').value,
        imageUrl: document.getElementById('prodImage').value
    };

    try {
        const res = await fetch(`${API_BASE}/products`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.status === 201) {
            showToast('Thêm sản phẩm mới thành công!', 'success');
            closeModal('addProductModal');
            document.getElementById('addProductForm').reset();
            await loadProducts();
            renderAdminProductsTable();
        } else {
            const err = await res.json();
            showToast(`Lỗi: ${err.message}`, 'error');
        }
    } catch (err) {
        showToast('Không thể tạo sản phẩm mới', 'error');
    }
}

function filterAdminOrders(status) {
    state.adminFilterStatus = status;
    loadAdminOrders();
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}
