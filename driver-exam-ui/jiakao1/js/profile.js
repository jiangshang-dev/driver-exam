// 个人中心页面专用JavaScript

// 个人中心页面初始化
function initPageData() {
    updateUserInfo();
    initUserProgress();
    initQuickFunctions();
    initVipSection();
}

// 更新用户信息显示
function updateUserInfo() {
    const userCard = document.querySelector('.user-card');
    const userAvatar = userCard?.querySelector('.user-avatar img');
    const username = userCard?.querySelector('.username');
    const userLevel = userCard?.querySelector('.user-level');
    const userStats = userCard?.querySelector('.user-stats');
    const loginBtn = userCard?.querySelector('.login-btn');
    
    if (isLoggedIn && currentUser) {
        // 已登录状态
        if (userAvatar) userAvatar.src = currentUser.avatar;
        if (username) username.textContent = currentUser.username;
        if (userLevel) userLevel.textContent = currentUser.vipLevel || '普通用户';
        if (userStats) {
            userStats.innerHTML = `
                <span>学习天数: ${currentUser.studyDays || 0}天</span>
                <span>正确率: ${currentUser.accuracy || 0}%</span>
            `;
        }
        if (loginBtn) loginBtn.style.display = 'none';
        
        // 显示VIP徽章
        const vipBadge = userCard?.querySelector('.vip-badge');
        if (vipBadge && currentUser.vipLevel === 'VIP') {
            vipBadge.style.display = 'block';
        }
    } else {
        // 未登录状态
        if (username) username.textContent = '未登录';
        if (userLevel) userLevel.textContent = '点击登录';
        if (userStats) userStats.innerHTML = '';
        if (loginBtn) loginBtn.style.display = 'flex';
        
        // 隐藏VIP徽章
        const vipBadge = userCard?.querySelector('.vip-badge');
        if (vipBadge) vipBadge.style.display = 'none';
    }
}

// 初始化用户进度
function initUserProgress() {
    const progressData = Storage.get('userProgress', {
        subject1: { completed: 750, total: 1000, accuracy: 92 },
        subject4: { completed: 225, total: 500, accuracy: 87 }
    });
    
    updateProgressDisplay(progressData);
}

// 更新进度显示
function updateProgressDisplay(progressData) {
    const progressItems = document.querySelectorAll('.progress-item');
    
    progressItems.forEach((item, index) => {
        const subject = index === 0 ? 'subject1' : 'subject4';
        const data = progressData[subject];
        
        if (!data) return;
        
        const percent = Math.round((data.completed / data.total) * 100);
        
        // 更新进度条
        const progressFill = item.querySelector('.progress-fill');
        const progressPercent = item.querySelector('.progress-percent');
        const progressDetails = item.querySelector('.progress-details');
        
        if (progressFill) {
            setTimeout(() => {
                progressFill.style.width = percent + '%';
            }, 500 + index * 200);
        }
        
        if (progressPercent) {
            progressPercent.textContent = percent + '%';
        }
        
        if (progressDetails) {
            progressDetails.innerHTML = `
                <span>已练习: ${data.completed}/${data.total}题</span>
                <span>正确率: ${data.accuracy}%</span>
            `;
        }
    });
}

// 初始化快捷功能
function initQuickFunctions() {
    const functionItems = document.querySelectorAll('.function-item');
    
    // 模拟数据
    const functionsData = {
        favorites: 156,
        wrongQuestions: 23,
        points: 2580,
        likes: 89
    };
    
    functionItems.forEach((item, index) => {
        const badge = item.querySelector('.function-badge');
        if (badge) {
            const keys = Object.keys(functionsData);
            if (keys[index]) {
                badge.textContent = functionsData[keys[index]];
            }
        }
    });
}

// 初始化VIP部分
function initVipSection() {
    const vipCard = document.querySelector('.vip-card');
    const vipContent = vipCard?.querySelector('.vip-content p');
    const renewBtn = vipCard?.querySelector('.vip-renew-btn');
    
    if (isLoggedIn && currentUser && currentUser.vipLevel === 'VIP') {
        if (vipContent) {
            vipContent.textContent = '您的VIP会员将于 2024-12-31 到期';
        }
        if (renewBtn) {
            renewBtn.textContent = '续费会员';
        }
    } else {
        if (vipContent) {
            vipContent.textContent = '开通VIP会员，享受专业学习服务';
        }
        if (renewBtn) {
            renewBtn.textContent = '立即开通';
        }
    }
}

// 显示登录弹窗
function showLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

// 关闭登录弹窗
function closeLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// 切换登录标签
function switchLoginTab(tabType) {
    // 更新标签状态
    document.querySelectorAll('.login-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`[onclick="switchLoginTab('${tabType}')"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 显示对应表单
    const phoneLogin = document.getElementById('phoneLogin');
    const thirdLogin = document.getElementById('thirdLogin');
    
    if (tabType === 'phone') {
        phoneLogin?.classList.remove('hidden');
        thirdLogin?.classList.add('hidden');
    } else {
        phoneLogin?.classList.add('hidden');
        thirdLogin?.classList.remove('hidden');
    }
}

// 获取验证码
function getVerificationCode() {
    const phoneInput = document.querySelector('#phoneLogin input[type="tel"]');
    const verifyBtn = document.querySelector('.verify-btn');
    
    if (!phoneInput || !phoneInput.value.trim()) {
        showToast('请输入手机号', 'warning');
        return;
    }
    
    if (!/^1[3-9]\d{9}$/.test(phoneInput.value.trim())) {
        showToast('请输入正确的手机号', 'warning');
        return;
    }
    
    // 模拟发送验证码
    if (verifyBtn) {
        verifyBtn.disabled = true;
        let countdown = 60;
        
        const timer = setInterval(() => {
            verifyBtn.textContent = `${countdown}s后重试`;
            countdown--;
            
            if (countdown < 0) {
                clearInterval(timer);
                verifyBtn.disabled = false;
                verifyBtn.textContent = '获取验证码';
            }
        }, 1000);
    }
    
    showToast('验证码已发送', 'success');
}

// 手机号登录
function loginWithPhone() {
    const phoneInput = document.querySelector('#phoneLogin input[type="tel"]');
    const codeInput = document.querySelector('#phoneLogin input[type="text"]');
    const checkbox = document.querySelector('#phoneLogin input[type="checkbox"]');
    
    const phone = phoneInput?.value.trim();
    const code = codeInput?.value.trim();
    
    if (!phone) {
        showToast('请输入手机号', 'warning');
        return;
    }
    
    if (!/^1[3-9]\d{9}$/.test(phone)) {
        showToast('请输入正确的手机号', 'warning');
        return;
    }
    
    if (!code) {
        showToast('请输入验证码', 'warning');
        return;
    }
    
    if (!checkbox?.checked) {
        showToast('请阅读并同意用户协议', 'warning');
        return;
    }
    
    // 执行登录
    simulateLogin({ phone, code })
        .then(() => {
            closeLoginModal();
            updateUserInfo();
            initUserProgress();
            initVipSection();
        })
        .catch(error => {
            console.error('Login failed:', error);
        });
}

// 第三方登录
function loginWith(platform) {
    const platformNames = {
        huawei: '华为账号',
        wechat: '微信',
        alipay: '支付宝'
    };
    
    const platformName = platformNames[platform] || platform;
    
    const loading = showLoading(`正在唤起${platformName}登录...`);
    
    setTimeout(() => {
        hideLoading();
        
        // 模拟第三方登录成功
        simulateLogin({ platform, userId: `${platform}_user_123` })
            .then(() => {
                closeLoginModal();
                updateUserInfo();
                initUserProgress();
                initVipSection();
            })
            .catch(error => {
                console.error('Third party login failed:', error);
            });
    }, 2000);
}

// 导航功能
function goToFavorites() {
    const page = document.getElementById('favoritesPage');
    if (page) {
        page.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }
}

function goToWrongQuestions() {
    showToast('错题本功能开发中...', 'info');
}

function goToPoints() {
    const page = document.getElementById('pointsPage');
    if (page) {
        page.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }
}

function goToLikes() {
    showToast('我的点赞功能开发中...', 'info');
}

// 系统功能导航
function goToHelp() {
    showToast('帮助中心功能开发中...', 'info');
}

function goToContact() {
    showToast('联系我们功能开发中...', 'info');
}

function goToAbout() {
    showToast('关于我们功能开发中...', 'info');
}

function goToAccountSecurity() {
    showToast('账号安全功能开发中...', 'info');
}

function goToFeedback() {
    showToast('意见反馈功能开发中...', 'info');
}

// VIP续费
function renewVip() {
    if (!isLoggedIn) {
        showLoginModal();
        return;
    }
    
    showToast('VIP续费功能开发中...', 'info');
}

// 关闭页面
function closePage(pageId) {
    const page = document.getElementById(pageId);
    if (page) {
        page.classList.add('hidden');
        document.body.style.overflow = 'auto';
    }
}

// 切换收藏标签
function switchFavoriteTab(tabType) {
    // 更新标签状态
    document.querySelectorAll('.favorite-tabs .tab-item').forEach(tab => {
        tab.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`[onclick="switchFavoriteTab('${tabType}')"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 显示对应内容
    const videoFavorites = document.getElementById('videoFavorites');
    const questionFavorites = document.getElementById('questionFavorites');
    
    if (tabType === 'videos') {
        videoFavorites?.classList.remove('hidden');
        questionFavorites?.classList.add('hidden');
    } else {
        videoFavorites?.classList.add('hidden');
        questionFavorites?.classList.remove('hidden');
    }
}

// 移除收藏
function removeFavorite(element) {
    if (confirm('确定要移除此收藏吗？')) {
        const item = element.closest('.favorite-item') || element.closest('.favorite-question');
        if (item) {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-100%)';
            
            setTimeout(() => {
                item.remove();
                showToast('已移除收藏', 'success');
            }, 300);
        }
    }
}

// 重写用户UI更新函数
function updateUserUI() {
    updateUserInfo();
    initVipSection();
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 监听登录弹窗点击事件
    const loginModal = document.getElementById('loginModal');
    if (loginModal) {
        loginModal.addEventListener('click', function(e) {
            if (e.target === loginModal) {
                closeLoginModal();
            }
        });
        
        // 监听登录按钮
        const loginSubmitBtn = loginModal.querySelector('.login-submit-btn');
        if (loginSubmitBtn) {
            loginSubmitBtn.addEventListener('click', loginWithPhone);
        }
        
        // 监听获取验证码按钮
        const verifyBtn = loginModal.querySelector('.verify-btn');
        if (verifyBtn) {
            verifyBtn.addEventListener('click', getVerificationCode);
        }
    }
    
    // 监听收藏页面点击事件
    const favoritesPage = document.getElementById('favoritesPage');
    if (favoritesPage) {
        favoritesPage.addEventListener('click', function(e) {
            if (e.target === favoritesPage) {
                closePage('favoritesPage');
            }
        });
    }
    
    // 监听积分页面点击事件
    const pointsPage = document.getElementById('pointsPage');
    if (pointsPage) {
        pointsPage.addEventListener('click', function(e) {
            if (e.target === pointsPage) {
                closePage('pointsPage');
            }
        });
    }
    
    // 监听Enter键登录
    document.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const activeForm = document.querySelector('#phoneLogin:not(.hidden)');
            if (activeForm && loginModal && loginModal.style.display === 'block') {
                loginWithPhone();
            }
        }
    });
    
    // 初始化签到功能
    initDailyCheckIn();
});

// 初始化每日签到
function initDailyCheckIn() {
    const lastCheckIn = Storage.get('lastCheckIn');
    const today = new Date().toDateString();
    
    if (lastCheckIn !== today) {
        // 显示签到提示
        setTimeout(() => {
            if (isLoggedIn) {
                showDailyCheckIn();
            }
        }, 2000);
    }
}

// 显示每日签到
function showDailyCheckIn() {
    const checkInToast = document.createElement('div');
    checkInToast.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        padding: 30px;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        text-align: center;
        z-index: 9999;
        animation: bounceIn 0.5s ease;
    `;
    
    checkInToast.innerHTML = `
        <div style="font-size: 48px; color: #FFD700; margin-bottom: 15px;">
            <i class="fas fa-gift"></i>
        </div>
        <h3 style="color: #333; margin-bottom: 10px;">每日签到</h3>
        <p style="color: #666; margin-bottom: 20px;">签到成功，获得10积分！</p>
        <button onclick="closeDailyCheckIn()" style="
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 10px 30px;
            border-radius: 20px;
            cursor: pointer;
        ">确定</button>
    `;
    
    document.body.appendChild(checkInToast);
    
    // 记录签到
    Storage.set('lastCheckIn', new Date().toDateString());
    
    // 更新积分
    if (currentUser) {
        currentUser.points = (currentUser.points || 0) + 10;
        Storage.set('currentUser', currentUser);
    }
}

// 关闭签到弹窗
function closeDailyCheckIn() {
    const checkIn = document.querySelector('[style*="bounceIn"]');
    if (checkIn) {
        checkIn.remove();
    }
}

// 添加bounceIn动画
if (!document.querySelector('#bounceInAnimation')) {
    const style = document.createElement('style');
    style.id = 'bounceInAnimation';
    style.textContent = `
        @keyframes bounceIn {
            0% { transform: translate(-50%, -50%) scale(0.3); opacity: 0; }
            50% { transform: translate(-50%, -50%) scale(1.05); }
            70% { transform: translate(-50%, -50%) scale(0.9); }
            100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
        }
    `;
    document.head.appendChild(style);
}