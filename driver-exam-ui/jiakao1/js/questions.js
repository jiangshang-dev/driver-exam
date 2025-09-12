// 刷题页面专用JavaScript

let currentSubject = 'subject1';
let currentQuestionIndex = 0;
let currentQuestionData = null;
let userAnswers = [];
let questionTimer = null;
let timeRemaining = 45;

// 题目数据模拟
const questionsData = {
    subject1: [
        {
            id: 1,
            type: 'single',
            question: '图中标志的含义是什么？',
            image: 'https://via.placeholder.com/300x200/E0E0E0/666?text=交通标志图片',
            options: [
                { label: 'A', text: '禁止通行' },
                { label: 'B', text: '减速慢行' },
                { label: 'C', text: '注意危险' },
                { label: 'D', text: '停车让行' }
            ],
            correctAnswer: 'A',
            explanation: '图中标志为禁止通行标志，表示禁止一切车辆和行人通行。此标志设在禁止通行的道路入口处。',
            chapter: '交通信号',
            difficulty: 'easy'
        },
        {
            id: 2,
            type: 'single',
            question: '在没有中心隔离设施或者没有中心线的道路上，机动车遇相对方向来车时应当怎样行驶？',
            options: [
                { label: 'A', text: '减速靠右行驶' },
                { label: 'B', text: '借对向车道行驶' },
                { label: 'C', text: '紧靠道路中心行驶' },
                { label: 'D', text: '靠路边停车让行' }
            ],
            correctAnswer: 'A',
            explanation: '在没有中心隔离设施或者没有中心线的道路上，机动车遇相对方向来车时应当减速靠右行驶，并与其他车辆、行人保持必要的安全距离。',
            chapter: '道路通行条件',
            difficulty: 'medium'
        }
    ]
};

// 刷题页面初始化
function initPageData() {
    initSubjectTabs();
    initChapterData();
    initQuestionPage();
}

// 初始化科目标签
function initSubjectTabs() {
    const tabs = document.querySelectorAll('.tab-item');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const subject = this.getAttribute('onclick').match(/'(.+)'/)[1];
            switchSubject(subject);
        });
    });
}

// 切换科目
function switchSubject(subject) {
    currentSubject = subject;
    
    // 更新标签状态
    document.querySelectorAll('.tab-item').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 找到对应的标签并添加active类
    const activeTab = document.querySelector(`[onclick="switchSubject('${subject}')"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 更新章节数据
    updateChapterData(subject);
    
    showToast(`已切换到${subject === 'subject1' ? '科目一' : '科目四'}`, 'info');
}

// 更新章节数据
function updateChapterData(subject) {
    // 这里可以根据科目更新章节信息
    const chapters = document.querySelectorAll('.chapter-item');
    chapters.forEach((chapter, index) => {
        const accuracy = chapter.querySelector('.accuracy');
        if (accuracy) {
            // 模拟不同科目的不同进度
            const rates = subject === 'subject1' ? ['85%', '72%', '未开始', '已完成'] : ['78%', '65%', '90%', '未开始'];
            accuracy.textContent = `正确率: ${rates[index]}`;
        }
    });
}

// 开始章节练习
function startChapter(chapterId) {
    const loading = showLoading('正在加载题目...');
    
    setTimeout(() => {
        hideLoading();
        // 初始化答题页面
        currentQuestionIndex = 0;
        userAnswers = [];
        loadQuestion(0);
        showQuestionPage();
    }, 1500);
}

// 快速练习
function quickPractice(type) {
    let message = '';
    switch(type) {
        case 'random':
            message = '开始随机练习';
            break;
        case 'sequential':
            message = '开始顺序练习';
            break;
        case 'wrong':
            message = '开始错题练习';
            break;
        case 'favorite':
            message = '开始收藏练习';
            break;
    }
    
    const loading = showLoading('正在加载题目...');
    
    setTimeout(() => {
        hideLoading();
        currentQuestionIndex = 0;
        userAnswers = [];
        loadQuestion(0);
        showQuestionPage();
        showToast(message, 'success');
    }, 1500);
}

// 显示答题页面
function showQuestionPage() {
    const questionPage = document.getElementById('questionPage');
    if (questionPage) {
        questionPage.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
        startQuestionTimer();
    }
}

// 关闭答题页面
function closeQuestion() {
    const questionPage = document.getElementById('questionPage');
    if (questionPage) {
        questionPage.classList.add('hidden');
        document.body.style.overflow = 'auto';
        stopQuestionTimer();
    }
}

// 加载题目
function loadQuestion(index) {
    const questions = questionsData[currentSubject] || [];
    if (index >= questions.length) {
        // 所有题目完成
        showToast('恭喜完成所有题目！', 'success');
        closeQuestion();
        return;
    }
    
    currentQuestionData = questions[index];
    currentQuestionIndex = index;
    
    // 更新题目进度
    const progressSpan = document.querySelector('.question-progress');
    if (progressSpan) {
        progressSpan.textContent = `${index + 1}/20`;
    }
    
    // 更新题目图片
    const questionImg = document.getElementById('questionImg');
    if (questionImg && currentQuestionData.image) {
        questionImg.src = currentQuestionData.image;
        questionImg.classList.remove('hidden');
    } else if (questionImg) {
        questionImg.classList.add('hidden');
    }
    
    // 更新题目文本
    const questionNumber = document.getElementById('questionNumber');
    const questionContent = document.getElementById('questionContent');
    
    if (questionNumber) {
        questionNumber.textContent = `${index + 1}.`;
    }
    
    if (questionContent) {
        questionContent.textContent = currentQuestionData.question;
    }
    
    // 更新选项
    updateQuestionOptions();
    
    // 隐藏答案解析
    const analysis = document.getElementById('answerAnalysis');
    if (analysis) {
        analysis.classList.add('hidden');
    }
    
    // 重置计时器
    resetQuestionTimer();
}

// 更新题目选项
function updateQuestionOptions() {
    const optionsContainer = document.getElementById('questionOptions');
    if (!optionsContainer || !currentQuestionData) return;
    
    optionsContainer.innerHTML = '';
    
    currentQuestionData.options.forEach(option => {
        const optionElement = document.createElement('div');
        optionElement.className = 'option-item';
        optionElement.onclick = () => selectOption(option.label);
        
        optionElement.innerHTML = `
            <span class="option-label">${option.label}</span>
            <span class="option-text">${option.text}</span>
        `;
        
        optionsContainer.appendChild(optionElement);
    });
}

// 选择选项
function selectOption(optionLabel) {
    // 移除之前的选中状态
    document.querySelectorAll('.option-item').forEach(item => {
        item.classList.remove('selected');
    });
    
    // 添加当前选中状态
    const selectedOption = document.querySelector(`[onclick="selectOption('${optionLabel}')"]`);
    if (selectedOption) {
        selectedOption.classList.add('selected');
    }
    
    // 记录用户答案
    userAnswers[currentQuestionIndex] = optionLabel;
    
    // 显示答案解析
    setTimeout(() => {
        showAnswerAnalysis(optionLabel);
    }, 500);
}

// 显示答案解析
function showAnswerAnalysis(userAnswer) {
    const analysis = document.getElementById('answerAnalysis');
    if (!analysis || !currentQuestionData) return;
    
    const isCorrect = userAnswer === currentQuestionData.correctAnswer;
    
    // 更新选项样式
    document.querySelectorAll('.option-item').forEach(item => {
        const label = item.querySelector('.option-label').textContent;
        
        if (label === currentQuestionData.correctAnswer) {
            item.classList.add('correct');
        } else if (label === userAnswer && !isCorrect) {
            item.classList.add('wrong');
        }
    });
    
    // 更新解析内容
    const resultIcon = analysis.querySelector('.correct-icon') || analysis.querySelector('.wrong-icon');
    const resultText = analysis.querySelector('.result-text');
    const analysisContent = analysis.querySelector('.analysis-content');
    
    if (resultIcon) {
        resultIcon.className = isCorrect ? 'fas fa-check-circle correct-icon' : 'fas fa-times-circle wrong-icon';
    }
    
    if (resultText) {
        resultText.textContent = isCorrect ? '回答正确！' : '回答错误！';
        resultText.className = isCorrect ? 'result-text' : 'result-text wrong';
    }
    
    if (analysisContent) {
        analysisContent.innerHTML = `
            <p><strong>正确答案：</strong>${currentQuestionData.correctAnswer}</p>
            <p><strong>解析：</strong>${currentQuestionData.explanation}</p>
        `;
    }
    
    // 显示解析
    analysis.classList.remove('hidden');
    
    // 停止计时器
    stopQuestionTimer();
}

// 下一题
function nextQuestion() {
    loadQuestion(currentQuestionIndex + 1);
}

// 上一题
function prevQuestion() {
    if (currentQuestionIndex > 0) {
        loadQuestion(currentQuestionIndex - 1);
    }
}

// 收藏题目
function bookmarkQuestion() {
    const bookmarkBtn = document.querySelector('.bookmark-question');
    if (bookmarkBtn) {
        bookmarkBtn.classList.toggle('active');
        const isBookmarked = bookmarkBtn.classList.contains('active');
        showToast(isBookmarked ? '已添加到收藏' : '已取消收藏', 'success');
    }
}

// 切换收藏状态（首页）
function toggleBookmark() {
    const bookmarkBtn = document.querySelector('.bookmark-btn');
    if (bookmarkBtn) {
        bookmarkBtn.classList.toggle('active');
        const isBookmarked = bookmarkBtn.classList.contains('active');
        showToast(isBookmarked ? '已添加到收藏' : '已取消收藏', 'success');
    }
}

// 题目计时器
function startQuestionTimer() {
    timeRemaining = 45;
    updateTimerDisplay();
    
    questionTimer = setInterval(() => {
        timeRemaining--;
        updateTimerDisplay();
        
        if (timeRemaining <= 0) {
            // 时间到，自动跳转下一题
            stopQuestionTimer();
            showToast('时间到！自动跳转下一题', 'warning');
            setTimeout(() => {
                nextQuestion();
            }, 1000);
        }
    }, 1000);
}

// 停止计时器
function stopQuestionTimer() {
    if (questionTimer) {
        clearInterval(questionTimer);
        questionTimer = null;
    }
}

// 重置计时器
function resetQuestionTimer() {
    stopQuestionTimer();
    startQuestionTimer();
}

// 更新计时器显示
function updateTimerDisplay() {
    const timerElement = document.getElementById('timer');
    if (timerElement) {
        const minutes = Math.floor(timeRemaining / 60);
        const seconds = timeRemaining % 60;
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // 时间不足时变红
        if (timeRemaining <= 10) {
            timerElement.style.color = '#f44336';
        } else {
            timerElement.style.color = '#666';
        }
    }
}

// 显示评论
function showComments() {
    const modal = document.getElementById('commentsModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

// 关闭评论
function closeComments() {
    const modal = document.getElementById('commentsModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// 点赞评论
function likeComment(button) {
    const likeBtn = button.closest('.like-btn');
    if (likeBtn) {
        likeBtn.style.color = '#667eea';
        showToast('已点赞', 'success');
    }
}

// 发布评论
function publishComment() {
    const textarea = document.querySelector('#commentsModal textarea');
    if (textarea && textarea.value.trim()) {
        showToast('评论发布成功', 'success');
        textarea.value = '';
    } else {
        showToast('请输入评论内容', 'warning');
    }
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 监听评论弹窗点击事件
    const commentsModal = document.getElementById('commentsModal');
    if (commentsModal) {
        commentsModal.addEventListener('click', function(e) {
            if (e.target === commentsModal) {
                closeComments();
            }
        });
        
        // 监听评论发布按钮
        const publishBtn = commentsModal.querySelector('.modal-body .btn');
        if (publishBtn) {
            publishBtn.addEventListener('click', publishComment);
        }
    }
    
    // 监听答题页面的键盘事件
    document.addEventListener('keydown', function(e) {
        const questionPage = document.getElementById('questionPage');
        if (questionPage && !questionPage.classList.contains('hidden')) {
            switch(e.key) {
                case 'ArrowLeft':
                    prevQuestion();
                    break;
                case 'ArrowRight':
                    nextQuestion();
                    break;
                case 'Escape':
                    closeQuestion();
                    break;
            }
        }
    });
});