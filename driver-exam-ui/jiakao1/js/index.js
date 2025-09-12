// 首页专用JavaScript

// 首页初始化
function initPageData() {
    initBanner();
    initRecommendedVideos();
    initStudyProgress();
}

// 初始化轮播图
function initBanner() {
    // 这里可以添加轮播图自动切换逻辑
    console.log('Banner initialized');
}

// 初始化推荐视频
function initRecommendedVideos() {
    // 从本地存储或API获取推荐视频数据
    const videos = Storage.get('recommendedVideos', [
        {
            id: 1,
            title: '交通标志详解',
            duration: 15,
            likes: 1200,
            favorites: 856,
            isVip: true
        },
        {
            id: 2,
            title: '科目一技巧总结',
            duration: 20,
            likes: 2100,
            favorites: 1300,
            isVip: true
        }
    ]);
    
    // 这里可以动态更新视频列表
    console.log('Recommended videos loaded:', videos);
}

// 初始化学习进度
function initStudyProgress() {
    const progress = Storage.get('studyProgress', {
        subject1: 75,
        subject4: 45
    });
    
    // 更新进度条动画
    setTimeout(() => {
        updateProgressBars(progress);
    }, 500);
}

// 更新进度条
function updateProgressBars(progress) {
    const progressBars = document.querySelectorAll('.progress-fill');
    progressBars.forEach((bar, index) => {
        const percent = index === 0 ? progress.subject1 : progress.subject4;
        bar.style.width = percent + '%';
    });
}

// 导航到视频页面
function goToVideos() {
    if (!checkVipAccess('videos')) {
        return;
    }
    showToast('视频功能开发中...', 'info');
}

// 导航到刷题页面
function goToQuestions() {
    window.location.href = 'questions.html';
}

// 导航到考试页面
function goToExam() {
    window.location.href = 'exam.html';
}

// 导航到错题本
function goToWrongQuestions() {
    showToast('错题本功能开发中...', 'info');
}

// 导航到收藏
function goToFavorites() {
    showToast('收藏功能开发中...', 'info');
}

// 导航到积分页面
function goToPoints() {
    showToast('积分功能开发中...', 'info');
}

// 播放视频
function playVideo(videoId) {
    if (!checkVipAccess('video_play')) {
        return;
    }
    
    // 模拟播放视频
    const loading = showLoading('加载视频中...');
    
    setTimeout(() => {
        hideLoading();
        showToast(`正在播放视频 ${videoId}`, 'success');
    }, 2000);
}

// 显示VIP弹窗
function showVipModal() {
    const modal = document.getElementById('vipModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

// 关闭VIP弹窗
function closeModal() {
    const modal = document.getElementById('vipModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// 购买VIP
function purchaseVip(plan) {
    const loading = showLoading('处理支付中...');
    
    mockApiRequest('/api/purchase-vip', { plan })
        .then(response => {
            hideLoading();
            if (response.success) {
                showToast('VIP购买成功！', 'success');
                closeModal();
                
                // 更新用户VIP状态
                if (currentUser) {
                    currentUser.vipLevel = 'VIP';
                    Storage.set('currentUser', currentUser);
                    updateUserUI();
                }
            }
        })
        .catch(error => {
            hideLoading();
            showToast('支付失败，请重试', 'error');
        });
}

// 重写用户UI更新函数
function updateUserUI() {
    // 更新VIP相关UI显示
    const vipBadges = document.querySelectorAll('.vip-badge');
    const userLevel = document.querySelector('.user-level');
    
    if (isLoggedIn && currentUser) {
        // 显示用户信息
        if (userLevel) {
            userLevel.textContent = currentUser.vipLevel || '普通用户';
        }
        
        // 根据VIP状态显示不同内容
        if (currentUser.vipLevel === 'VIP') {
            vipBadges.forEach(badge => {
                badge.style.display = 'block';
            });
        }
    } else {
        // 未登录状态
        vipBadges.forEach(badge => {
            badge.style.display = 'block'; // 显示VIP标识以提示用户
        });
    }
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 监听VIP弹窗点击事件
    const modal = document.getElementById('vipModal');
    if (modal) {
        // 点击背景关闭弹窗
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });
        
        // 添加VIP购买按钮事件
        const vipBtn = modal.querySelector('.vip-btn');
        if (vipBtn) {
            vipBtn.addEventListener('click', function() {
                purchaseVip('year'); // 默认购买年卡
            });
        }
    }
    
    // 初始化轮播图自动播放
    initBannerAutoPlay();
});

// 轮播图自动播放
function initBannerAutoPlay() {
    const bannerItems = document.querySelectorAll('.banner-item');
    if (bannerItems.length <= 1) return;
    
    let currentIndex = 0;
    
    setInterval(() => {
        // 移除当前active类
        bannerItems[currentIndex].classList.remove('active');
        
        // 切换到下一张
        currentIndex = (currentIndex + 1) % bannerItems.length;
        bannerItems[currentIndex].classList.add('active');
    }, 5000); // 5秒切换一次
}

// 搜索功能
function performSearch() {
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        const query = searchInput.value.trim();
        if (query) {
            showToast(`搜索: ${query}`, 'info');
            // 这里可以实现实际的搜索逻辑
        }
    }
}

// 点击搜索图标
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('search-icon')) {
        performSearch();
    }
});

// 监听回车键搜索
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        const searchInput = document.querySelector('.search-input');
        if (searchInput && document.activeElement === searchInput) {
            performSearch();
        }
    }
});