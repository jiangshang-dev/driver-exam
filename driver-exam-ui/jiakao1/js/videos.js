// 视频页面专用JavaScript

let currentVideoId = null;
let currentCategory = 'all';
let videoLikes = {};
let videoFavorites = {};

// 视频数据模拟
const videosData = {
    1: {
        id: 1,
        title: '交通标志详解 - 禁令标志篇',
        description: '详细讲解各类禁令标志的含义和应用场景，帮助学员快速掌握交通标志知识。',
        instructor: '张教练',
        duration: '15:30',
        views: 12000,
        likes: 856,
        category: 'signs',
        isVip: true,
        videoUrl: '#'
    },
    2: {
        id: 2,
        title: '科目一高分技巧总结',
        description: '总结科目一考试重点，分享高分通过技巧，提高考试通过率。',
        instructor: '李教练',
        duration: '22:15',
        views: 21000,
        likes: 1300,
        category: 'subject1',
        isVip: true,
        videoUrl: '#'
    },
    3: {
        id: 3,
        title: '安全文明驾驶常识',
        description: '科目四安全文明驾驶知识点详解，掌握安全驾驶要领。',
        instructor: '王教练',
        duration: '18:45',
        views: 8500,
        likes: 642,
        category: 'subject4',
        isVip: true,
        videoUrl: '#'
    },
    4: {
        id: 4,
        title: '驾考基础知识入门',
        description: '驾考新手必看的基础知识介绍，为学车打好基础。',
        instructor: '驾考通',
        duration: '05:30',
        views: 52000,
        likes: 2100,
        category: 'all',
        isVip: false,
        videoUrl: '#'
    }
};

// 视频页面初始化
function initPageData() {
    initVideoLikes();
    initVideoFavorites();
    updateVipBanner();
    loadVideoList();
}

// 初始化视频点赞状态
function initVideoLikes() {
    videoLikes = Storage.get('videoLikes', {});
}

// 初始化视频收藏状态
function initVideoFavorites() {
    videoFavorites = Storage.get('videoFavorites', {});
}

// 更新VIP横幅显示
function updateVipBanner() {
    const vipBanner = document.querySelector('.vip-banner');
    const vipText = vipBanner?.querySelector('.vip-text h3');
    const vipBtn = vipBanner?.querySelector('.vip-upgrade-btn');
    
    if (isLoggedIn && currentUser && currentUser.vipLevel === 'VIP') {
        if (vipText) {
            vipText.textContent = '尊享VIP专业课程';
        }
        if (vipBtn) {
            vipBtn.textContent = '管理会员';
            vipBtn.onclick = () => showToast('会员管理功能开发中...', 'info');
        }
    } else {
        if (vipText) {
            vipText.textContent = '开通VIP享受专业视频课程';
        }
        if (vipBtn) {
            vipBtn.textContent = '立即开通';
            vipBtn.onclick = () => showVipModal();
        }
    }
}

// 加载视频列表
function loadVideoList() {
    const videoGrid = document.querySelector('.video-grid');
    if (!videoGrid) return;
    
    // 根据分类筛选视频
    const filteredVideos = Object.values(videosData).filter(video => {
        if (currentCategory === 'all') return true;
        return video.category === currentCategory;
    });
    
    // 清空现有列表
    videoGrid.innerHTML = '';
    
    // 生成视频卡片
    filteredVideos.forEach(video => {
        const videoCard = createVideoCard(video);
        videoGrid.appendChild(videoCard);
    });
}

// 创建视频卡片
function createVideoCard(video) {
    const card = document.createElement('div');
    card.className = `video-card ${!video.isVip ? 'free-video' : ''}`;
    card.onclick = () => playVideo(video.id);
    
    const isLiked = videoLikes[video.id] || false;
    const isFavorited = videoFavorites[video.id] || false;
    
    card.innerHTML = `
        <div class="video-thumbnail">
            <img src="https://via.placeholder.com/300x180/${getRandomColor()}/white?text=${encodeURIComponent(video.title.substring(0, 6))}" alt="视频缩略图">
            <div class="video-duration">${video.duration}</div>
            ${video.isVip ? `
                <div class="vip-overlay">
                    <i class="fas fa-lock"></i>
                    <span>VIP专享</span>
                </div>
            ` : `
                <div class="free-badge">免费</div>
            `}
            <div class="play-btn">
                <i class="fas fa-play"></i>
            </div>
        </div>
        <div class="video-info">
            <h4 class="video-title">${video.title}</h4>
            <p class="video-description">${video.description}</p>
            <div class="video-meta">
                <span class="video-instructor">
                    <i class="fas fa-user"></i>
                    ${video.instructor}
                </span>
                <span class="video-stats">
                    <i class="fas fa-eye"></i>
                    ${formatNumber(video.views)}
                </span>
                <span class="video-likes">
                    <i class="fas fa-thumbs-up"></i>
                    ${formatNumber(video.likes)}
                </span>
            </div>
            <div class="video-actions">
                <button class="action-btn ${isLiked ? 'liked' : ''}" onclick="event.stopPropagation(); likeVideo(${video.id})">
                    <i class="fas fa-thumbs-up"></i>
                </button>
                <button class="action-btn ${isFavorited ? 'favorited' : ''}" onclick="event.stopPropagation(); favoriteVideo(${video.id})">
                    <i class="fas fa-heart"></i>
                </button>
                <button class="action-btn" onclick="event.stopPropagation(); shareVideo(${video.id})">
                    <i class="fas fa-share"></i>
                </button>
            </div>
        </div>
    `;
    
    return card;
}

// 获取随机颜色
function getRandomColor() {
    const colors = ['2196F3', 'FF9800', '4CAF50', '9C27B0', 'F44336', '00BCD4'];
    return colors[Math.floor(Math.random() * colors.length)];
}

// 格式化数字
function formatNumber(num) {
    if (num >= 10000) {
        return Math.floor(num / 1000) / 10 + '万';
    } else if (num >= 1000) {
        return Math.floor(num / 100) / 10 + 'k';
    }
    return num.toString();
}

// 切换分类
function switchCategory(category) {
    currentCategory = category;
    
    // 更新标签状态
    document.querySelectorAll('.tab-item').forEach(tab => {
        tab.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`[onclick="switchCategory('${category}')"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 重新加载视频列表
    loadVideoList();
    
    const categoryNames = {
        all: '全部',
        subject1: '科目一',
        subject4: '科目四',
        signs: '交通标志',
        rules: '交通规则',
        skills: '考试技巧'
    };
    
    showToast(`已切换到${categoryNames[category]}分类`, 'info');
}

// 播放视频
function playVideo(videoId) {
    const video = videosData[videoId];
    if (!video) return;
    
    // 检查VIP权限
    if (video.isVip && !checkVipAccess('video_play')) {
        return;
    }
    
    currentVideoId = videoId;
    
    // 显示视频播放器
    showVideoPlayer(video);
}

// 显示视频播放器
function showVideoPlayer(video) {
    const player = document.getElementById('videoPlayer');
    if (!player) return;
    
    // 更新播放器内容
    updatePlayerContent(video);
    
    // 显示播放器
    player.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    
    // 模拟视频加载
    const loading = showLoading('正在加载视频...');
    
    setTimeout(() => {
        hideLoading();
        showToast('视频加载完成', 'success');
    }, 2000);
}

// 更新播放器内容
function updatePlayerContent(video) {
    // 更新标题
    const playerTitle = document.getElementById('playerTitle');
    const videoTitle = document.getElementById('videoTitle');
    
    if (playerTitle) playerTitle.textContent = video.title;
    if (videoTitle) videoTitle.textContent = video.title;
    
    // 更新视频详情
    const videoDesc = document.querySelector('.video-desc');
    const instructor = document.querySelector('.instructor');
    const stats = document.querySelector('.stats');
    
    if (videoDesc) videoDesc.textContent = video.description;
    if (instructor) instructor.textContent = video.instructor;
    if (stats) {
        stats.innerHTML = `
            <span><i class="fas fa-eye"></i> ${formatNumber(video.views)}</span>
            <span><i class="fas fa-thumbs-up"></i> ${formatNumber(video.likes)}</span>
            <span><i class="fas fa-clock"></i> 2024-01-15</span>
        `;
    }
    
    // 更新收藏状态
    const playerFavorite = document.querySelector('.player-favorite');
    if (playerFavorite) {
        playerFavorite.classList.toggle('active', videoFavorites[video.id] || false);
    }
    
    // 更新操作按钮状态
    updatePlayerActionButtons(video.id);
}

// 更新播放器操作按钮状态
function updatePlayerActionButtons(videoId) {
    const actionButtons = document.querySelectorAll('.action-button');
    actionButtons.forEach(button => {
        const icon = button.querySelector('i');
        const text = button.querySelector('span').textContent;
        
        if (text === '点赞') {
            button.classList.toggle('active', videoLikes[videoId] || false);
        } else if (text === '收藏') {
            button.classList.toggle('active', videoFavorites[videoId] || false);
        }
    });
}

// 关闭视频播放器
function closeVideoPlayer() {
    const player = document.getElementById('videoPlayer');
    if (player) {
        player.classList.add('hidden');
        document.body.style.overflow = 'auto';
        
        // 停止视频播放
        const videoElement = document.getElementById('videoElement');
        if (videoElement) {
            videoElement.pause();
            videoElement.currentTime = 0;
        }
    }
    
    currentVideoId = null;
}

// 点赞视频
function likeVideo(videoId) {
    const isLiked = videoLikes[videoId] || false;
    videoLikes[videoId] = !isLiked;
    
    // 保存到本地存储
    Storage.set('videoLikes', videoLikes);
    
    // 更新UI
    updateVideoActionButtons(videoId);
    
    // 更新视频数据
    if (videosData[videoId]) {
        videosData[videoId].likes += isLiked ? -1 : 1;
    }
    
    showToast(isLiked ? '已取消点赞' : '已点赞', 'success');
    
    // 如果是在播放器中，同时更新播放器状态
    if (currentVideoId === videoId) {
        updatePlayerActionButtons(videoId);
    }
}

// 收藏视频
function favoriteVideo(videoId) {
    const isFavorited = videoFavorites[videoId] || false;
    videoFavorites[videoId] = !isFavorited;
    
    // 保存到本地存储
    Storage.set('videoFavorites', videoFavorites);
    
    // 更新UI
    updateVideoActionButtons(videoId);
    
    showToast(isFavorited ? '已取消收藏' : '已收藏', 'success');
    
    // 如果是在播放器中，同时更新播放器状态
    if (currentVideoId === videoId) {
        updatePlayerActionButtons(videoId);
        const playerFavorite = document.querySelector('.player-favorite');
        if (playerFavorite) {
            playerFavorite.classList.toggle('active', !isFavorited);
        }
    }
}

// 分享视频
function shareVideo(videoId) {
    const video = videosData[videoId];
    if (!video) return;
    
    // 模拟分享功能
    showToast('分享功能开发中...', 'info');
}

// 更新视频操作按钮状态
function updateVideoActionButtons(videoId) {
    const videoCard = document.querySelector(`[onclick="playVideo(${videoId})"]`);
    if (!videoCard) return;
    
    const likeBtn = videoCard.querySelector('.action-btn:first-child');
    const favoriteBtn = videoCard.querySelector('.action-btn:nth-child(2)');
    
    if (likeBtn) {
        likeBtn.classList.toggle('liked', videoLikes[videoId] || false);
    }
    
    if (favoriteBtn) {
        favoriteBtn.classList.toggle('favorited', videoFavorites[videoId] || false);
    }
}

// 播放器中的点赞
function likeCurrentVideo() {
    if (currentVideoId) {
        likeVideo(currentVideoId);
    }
}

// 播放器中的收藏
function favoriteCurrentVideo() {
    if (currentVideoId) {
        favoriteVideo(currentVideoId);
    }
}

// 播放器中的分享
function shareCurrentVideo() {
    if (currentVideoId) {
        shareVideo(currentVideoId);
    }
}

// 切换播放器收藏状态
function togglePlayerFavorite() {
    if (currentVideoId) {
        favoriteVideo(currentVideoId);
    }
}

// 显示视频评论
function showVideoComments() {
    const commentsSection = document.querySelector('.comments-section');
    if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' });
    }
}

// 提交视频评论
function submitVideoComment() {
    const commentInput = document.getElementById('commentInput');
    if (!commentInput) return;
    
    const content = commentInput.value.trim();
    if (!content) {
        showToast('请输入评论内容', 'warning');
        return;
    }
    
    if (!isLoggedIn) {
        showToast('请先登录', 'warning');
        return;
    }
    
    // 模拟提交评论
    const loading = showLoading('发布评论中...');
    
    setTimeout(() => {
        hideLoading();
        showToast('评论发布成功', 'success');
        commentInput.value = '';
        
        // 这里可以添加新评论到评论列表
        addNewComment(content);
    }, 1500);
}

// 添加新评论
function addNewComment(content) {
    const commentsList = document.querySelector('.comments-list');
    if (!commentsList) return;
    
    const newComment = document.createElement('div');
    newComment.className = 'comment-item';
    newComment.innerHTML = `
        <div class="comment-avatar">
            <i class="fas fa-user-circle"></i>
        </div>
        <div class="comment-content">
            <div class="comment-header">
                <span class="comment-author">${currentUser?.username || '匿名用户'}</span>
                <span class="comment-time">刚刚</span>
            </div>
            <p class="comment-text">${content}</p>
            <div class="comment-actions">
                <button class="comment-like">
                    <i class="fas fa-thumbs-up"></i>
                    <span>0</span>
                </button>
                <button class="comment-reply">回复</button>
            </div>
        </div>
    `;
    
    // 插入到评论列表顶部
    commentsList.insertBefore(newComment, commentsList.firstChild);
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
function closeVipModal() {
    const modal = document.getElementById('vipModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// 购买VIP
function purchaseVip() {
    const loading = showLoading('处理支付中...');
    
    mockApiRequest('/api/purchase-vip', { plan: 'year' })
        .then(response => {
            hideLoading();
            if (response.success) {
                showToast('VIP购买成功！', 'success');
                closeVipModal();
                
                // 更新用户VIP状态
                if (currentUser) {
                    currentUser.vipLevel = 'VIP';
                    Storage.set('currentUser', currentUser);
                    updateVipBanner();
                }
            }
        })
        .catch(error => {
            hideLoading();
            showToast('支付失败，请重试', 'error');
        });
}

// 显示搜索
function showSearch() {
    showToast('搜索功能开发中...', 'info');
}

// 重写用户UI更新函数
function updateUserUI() {
    updateVipBanner();
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 监听VIP弹窗点击事件
    const vipModal = document.getElementById('vipModal');
    if (vipModal) {
        vipModal.addEventListener('click', function(e) {
            if (e.target === vipModal) {
                closeVipModal();
            }
        });
    }
    
    // 监听视频播放器点击事件
    const videoPlayer = document.getElementById('videoPlayer');
    if (videoPlayer) {
        // 阻止背景点击关闭播放器
        videoPlayer.addEventListener('click', function(e) {
            // 只有点击播放器背景时才关闭
            if (e.target === videoPlayer) {
                closeVideoPlayer();
            }
        });
    }
    
    // 监听键盘事件
    document.addEventListener('keydown', function(e) {
        const videoPlayer = document.getElementById('videoPlayer');
        if (videoPlayer && !videoPlayer.classList.contains('hidden')) {
            switch(e.key) {
                case 'Escape':
                    closeVideoPlayer();
                    break;
                case ' ':
                    e.preventDefault();
                    const video = document.getElementById('videoElement');
                    if (video) {
                        if (video.paused) {
                            video.play();
                        } else {
                            video.pause();
                        }
                    }
                    break;
            }
        }
    });
    
    // 监听评论点赞事件
    document.addEventListener('click', function(e) {
        if (e.target.closest('.comment-like')) {
            const button = e.target.closest('.comment-like');
            const span = button.querySelector('span');
            const currentLikes = parseInt(span.textContent) || 0;
            span.textContent = currentLikes + 1;
            button.style.color = '#667eea';
            showToast('已点赞评论', 'success');
        }
    });
});