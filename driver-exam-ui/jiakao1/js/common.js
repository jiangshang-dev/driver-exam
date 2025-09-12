// 通用JavaScript功能

// 全局变量
let currentUser = null;
let isLoggedIn = false;

// 页面导航功能
function switchTab(tab) {
    // 移除所有导航项的active类
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // 根据tab参数导航到对应页面
    switch(tab) {
        case 'home':
            window.location.href = 'index.html';
            break;
        case 'study':
            window.location.href = 'questions.html';
            break;
        case 'exam':
            window.location.href = 'exam.html';
            break;
        case 'profile':
            window.location.href = 'profile.html';
            break;
    }
}

// 返回功能
function goBack() {
    window.history.back();
}

// 跳转到个人中心
function goToProfile() {
    window.location.href = 'profile.html';
}

// 跳转到设置页面
function goToSettings() {
    // 这里可以实现设置页面
    showToast('设置功能开发中...');
}

// 显示提示信息
function showToast(message, type = 'info') {
    // 创建toast元素
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    
    // 添加样式
    toast.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: ${type === 'error' ? '#f44336' : type === 'success' ? '#4CAF50' : '#667eea'};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        font-size: 14px;
        z-index: 9999;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    // 显示动画
    setTimeout(() => {
        toast.style.opacity = '1';
    }, 100);
    
    // 3秒后自动消失
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// 显示加载中
function showLoading(message = '加载中...') {
    const loading = document.createElement('div');
    loading.id = 'globalLoading';
    loading.innerHTML = `
        <div style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        ">
            <div style="
                background: white;
                padding: 30px;
                border-radius: 10px;
                text-align: center;
                min-width: 120px;
            ">
                <div style="
                    width: 30px;
                    height: 30px;
                    border: 3px solid #f0f0f0;
                    border-top: 3px solid #667eea;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                    margin: 0 auto 15px;
                "></div>
                <div style="color: #666; font-size: 14px;">${message}</div>
            </div>
        </div>
    `;
    
    // 添加旋转动画
    if (!document.querySelector('#spinAnimation')) {
        const style = document.createElement('style');
        style.id = 'spinAnimation';
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }
    
    document.body.appendChild(loading);
    return loading;
}

// 隐藏加载中
function hideLoading() {
    const loading = document.getElementById('globalLoading');
    if (loading) {
        loading.remove();
    }
}

// 模拟API请求
function mockApiRequest(url, data = null, delay = 1000) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            // 模拟成功响应
            if (Math.random() > 0.1) { // 90%成功率
                resolve({
                    success: true,
                    data: data || { message: '操作成功' },
                    code: 200
                });
            } else {
                reject({
                    success: false,
                    message: '网络错误，请重试',
                    code: 500
                });
            }
        }, delay);
    });
}

// 格式化时间
function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
}

// 格式化日期
function formatDate(date) {
    const now = new Date();
    const targetDate = new Date(date);
    const diffTime = now - targetDate;
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
        return '今天';
    } else if (diffDays === 1) {
        return '昨天';
    } else if (diffDays < 7) {
        return `${diffDays}天前`;
    } else {
        return targetDate.toLocaleDateString('zh-CN');
    }
}

// 本地存储工具
const Storage = {
    set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('Storage set error:', e);
        }
    },
    
    get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (e) {
            console.error('Storage get error:', e);
            return defaultValue;
        }
    },
    
    remove(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.error('Storage remove error:', e);
        }
    },
    
    clear() {
        try {
            localStorage.clear();
        } catch (e) {
            console.error('Storage clear error:', e);
        }
    }
};

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 节流函数
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 初始化当前页面导航高亮
    initCurrentNavigation();
    
    // 初始化用户登录状态
    initUserStatus();
    
    // 初始化页面数据
    initPageData();
});

// 初始化当前页面导航
function initCurrentNavigation() {
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.classList.remove('active');
    });
    
    // 根据当前页面设置active状态
    let activeIndex = 0;
    switch(currentPage) {
        case 'index.html':
        case '':
            activeIndex = 0;
            break;
        case 'questions.html':
            activeIndex = 1;
            break;
        case 'exam.html':
            activeIndex = 2;
            break;
        case 'profile.html':
            activeIndex = 3;
            break;
    }
    
    if (navItems[activeIndex]) {
        navItems[activeIndex].classList.add('active');
    }
}

// 初始化用户状态
function initUserStatus() {
    // 从本地存储获取用户信息
    currentUser = Storage.get('currentUser');
    isLoggedIn = !!currentUser;
    
    // 更新UI显示
    updateUserUI();
}

// 更新用户UI
function updateUserUI() {
    // 这个函数在各个页面的具体实现中会被重写
}

// 初始化页面数据
function initPageData() {
    // 这个函数在各个页面的具体实现中会被重写
}

// 模拟用户登录
function simulateLogin(loginData) {
    const loading = showLoading('登录中...');
    
    return mockApiRequest('/api/login', loginData, 2000)
        .then(response => {
            hideLoading();
            if (response.success) {
                // 模拟用户数据
                currentUser = {
                    id: 1,
                    username: '驾考学员',
                    phone: loginData.phone || '138****8888',
                    avatar: 'https://via.placeholder.com/80x80/667eea/white?text=用户',
                    vipLevel: 'VIP',
                    studyDays: 45,
                    accuracy: 89,
                    points: 2580
                };
                
                isLoggedIn = true;
                Storage.set('currentUser', currentUser);
                updateUserUI();
                showToast('登录成功！', 'success');
                return currentUser;
            } else {
                throw new Error(response.message || '登录失败');
            }
        })
        .catch(error => {
            hideLoading();
            showToast(error.message || '登录失败，请重试', 'error');
            throw error;
        });
}

// 用户登出
function logout() {
    currentUser = null;
    isLoggedIn = false;
    Storage.remove('currentUser');
    updateUserUI();
    showToast('已退出登录', 'info');
}

// 检查是否需要VIP权限
function checkVipAccess(feature) {
    if (!isLoggedIn) {
        showToast('请先登录', 'error');
        return false;
    }
    
    if (!currentUser.vipLevel || currentUser.vipLevel === 'normal') {
        showVipModal();
        return false;
    }
    
    return true;
}

// 显示VIP弹窗（在各页面中实现）
function showVipModal() {
    // 这个函数在各个页面中会有具体实现
    showToast('该功能需要VIP会员', 'error');
}