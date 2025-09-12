// 模拟考试页面专用JavaScript

let currentExamType = '';
let examQuestions = [];
let currentExamQuestionIndex = 0;
let examAnswers = [];
let examTimer = null;
let examTimeRemaining = 0;
let examStartTime = null;

// 考试题目数据模拟
const examQuestionsData = {
    subject1: [
        {
            id: 1,
            question: '在没有中心隔离设施或者没有中心线的道路上，机动车遇相对方向来车时应当怎样行驶？',
            options: [
                { label: 'A', text: '减速靠右行驶' },
                { label: 'B', text: '借对向车道行驶' },
                { label: 'C', text: '紧靠道路中心行驶' },
                { label: 'D', text: '靠路边停车让行' }
            ],
            correctAnswer: 'A',
            explanation: '在没有中心隔离设施或者没有中心线的道路上，机动车遇相对方向来车时应当减速靠右行驶。'
        },
        {
            id: 2,
            question: '驾驶机动车在道路上违反道路交通安全法的行为，属于什么行为？',
            options: [
                { label: 'A', text: '违章行为' },
                { label: 'B', text: '违法行为' },
                { label: 'C', text: '过失行为' },
                { label: 'D', text: '违规行为' }
            ],
            correctAnswer: 'B',
            explanation: '根据《道路交通安全法》，违反交通安全法的行为属于违法行为。'
        }
        // 这里应该有100道题，为了演示只放2道
    ],
    subject4: [
        {
            id: 1,
            question: '雨天驾驶机动车需要注意什么？',
            options: [
                { label: 'A', text: '及时开启雨刷器' },
                { label: 'B', text: '注意避让行人' },
                { label: 'C', text: '减速慢行' },
                { label: 'D', text: '以上都是' }
            ],
            correctAnswer: 'D',
            explanation: '雨天驾驶需要开启雨刷器、注意避让行人、减速慢行等多项注意事项。'
        }
        // 这里应该有50道题，为了演示只放1道
    ]
};

// 模拟考试页面初始化
function initPageData() {
    initExamHistory();
    showExamSelection();
}

// 初始化考试历史
function initExamHistory() {
    const historyData = Storage.get('examHistory', [
        {
            subject: '科目一模拟考试',
            time: '2024-01-15 14:30',
            score: 92,
            passed: true
        },
        {
            subject: '科目四模拟考试',
            time: '2024-01-14 09:15',
            score: 85,
            passed: false
        },
        {
            subject: '科目一模拟考试',
            time: '2024-01-13 16:45',
            score: 96,
            passed: true
        }
    ]);
    
    updateHistoryDisplay(historyData);
}

// 更新历史记录显示
function updateHistoryDisplay(historyData) {
    const historyList = document.querySelector('.history-list');
    if (!historyList) return;
    
    historyList.innerHTML = '';
    
    historyData.forEach(record => {
        const item = document.createElement('div');
        item.className = 'history-item';
        
        item.innerHTML = `
            <div class="history-info">
                <div class="exam-subject">${record.subject}</div>
                <div class="exam-time">${formatDate(record.time)}</div>
            </div>
            <div class="exam-score ${record.passed ? 'passed' : 'failed'}">
                <span class="score">${record.score}</span>
                <span class="status">${record.passed ? '合格' : '不合格'}</span>
            </div>
        `;
        
        historyList.appendChild(item);
    });
}

// 开始考试
function startExam(examType) {
    currentExamType = examType;
    const subjectName = examType === 'subject1' ? '科目一' : '科目四';
    
    // 确认开始考试
    if (!confirm(`确定要开始${subjectName}模拟考试吗？考试开始后不能暂停。`)) {
        return;
    }
    
    const loading = showLoading('正在准备考试...');
    
    setTimeout(() => {
        hideLoading();
        initExam(examType);
    }, 2000);
}

// 初始化考试
function initExam(examType) {
    // 获取考试题目
    examQuestions = [...(examQuestionsData[examType] || [])];
    
    // 打乱题目顺序
    shuffleArray(examQuestions);
    
    // 初始化考试状态
    currentExamQuestionIndex = 0;
    examAnswers = new Array(examQuestions.length).fill(null);
    examStartTime = new Date();
    
    // 设置考试时间
    examTimeRemaining = examType === 'subject1' ? 45 * 60 : 30 * 60; // 45分钟或30分钟
    
    // 显示考试页面
    showExamPage();
    
    // 初始化题目导航
    initQuestionNavigation();
    
    // 加载第一题
    loadExamQuestion(0);
    
    // 开始计时
    startExamTimer();
}

// 打乱数组
function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
}

// 显示考试页面
function showExamPage() {
    const examSelection = document.getElementById('examSelection');
    const examPage = document.getElementById('examPage');
    
    if (examSelection) examSelection.classList.add('hidden');
    if (examPage) examPage.classList.remove('hidden');
    
    // 更新考试科目名称
    const subjectName = document.querySelector('.exam-subject-name');
    if (subjectName) {
        subjectName.textContent = currentExamType === 'subject1' ? '科目一模拟考试' : '科目四模拟考试';
    }
    
    // 更新题目总数
    const totalQuestions = document.getElementById('totalQuestions');
    if (totalQuestions) {
        totalQuestions.textContent = examQuestions.length;
    }
}

// 显示考试选择页面
function showExamSelection() {
    const examSelection = document.getElementById('examSelection');
    const examPage = document.getElementById('examPage');
    const examResult = document.getElementById('examResult');
    
    if (examSelection) examSelection.classList.remove('hidden');
    if (examPage) examPage.classList.add('hidden');
    if (examResult) examResult.classList.add('hidden');
}

// 初始化题目导航
function initQuestionNavigation() {
    const navGrid = document.getElementById('questionNavGrid');
    if (!navGrid) return;
    
    navGrid.innerHTML = '';
    
    examQuestions.forEach((_, index) => {
        const navItem = document.createElement('div');
        navItem.className = 'nav-question unanswered';
        navItem.textContent = index + 1;
        navItem.onclick = () => loadExamQuestion(index);
        navGrid.appendChild(navItem);
    });
}

// 加载考试题目
function loadExamQuestion(index) {
    if (index < 0 || index >= examQuestions.length) return;
    
    currentExamQuestionIndex = index;
    const question = examQuestions[index];
    
    // 更新当前题号
    const currentNum = document.getElementById('currentQuestionNum');
    if (currentNum) {
        currentNum.textContent = index + 1;
    }
    
    // 更新题目导航状态
    updateQuestionNavigation();
    
    // 更新题目内容
    const questionNumber = document.getElementById('questionNumber');
    const questionContent = document.getElementById('questionContent');
    
    if (questionNumber) {
        questionNumber.textContent = `${index + 1}.`;
    }
    
    if (questionContent) {
        questionContent.textContent = question.question;
    }
    
    // 更新题目图片（如果有）
    const questionImg = document.getElementById('questionImg');
    if (questionImg) {
        if (question.image) {
            questionImg.src = question.image;
            questionImg.classList.remove('hidden');
        } else {
            questionImg.classList.add('hidden');
        }
    }
    
    // 更新选项
    updateExamQuestionOptions(question, index);
    
    // 更新按钮状态
    updateControlButtons();
}

// 更新考试题目选项
function updateExamQuestionOptions(question, index) {
    const optionsContainer = document.getElementById('questionOptions');
    if (!optionsContainer) return;
    
    optionsContainer.innerHTML = '';
    
    question.options.forEach(option => {
        const optionElement = document.createElement('div');
        optionElement.className = 'option-item';
        optionElement.onclick = () => selectExamOption(option.label, index);
        
        // 如果已经选择过，显示选中状态
        if (examAnswers[index] === option.label) {
            optionElement.classList.add('selected');
        }
        
        optionElement.innerHTML = `
            <span class="option-label">${option.label}</span>
            <span class="option-text">${option.text}</span>
        `;
        
        optionsContainer.appendChild(optionElement);
    });
}

// 选择考试选项
function selectExamOption(optionLabel, questionIndex) {
    // 记录答案
    examAnswers[questionIndex] = optionLabel;
    
    // 更新选项显示
    const question = examQuestions[questionIndex];
    updateExamQuestionOptions(question, questionIndex);
    
    // 更新题目导航状态
    updateQuestionNavigation();
}

// 更新题目导航状态
function updateQuestionNavigation() {
    const navItems = document.querySelectorAll('.nav-question');
    
    navItems.forEach((item, index) => {
        item.classList.remove('current', 'answered', 'unanswered');
        
        if (index === currentExamQuestionIndex) {
            item.classList.add('current');
        } else if (examAnswers[index] !== null) {
            item.classList.add('answered');
        } else {
            item.classList.add('unanswered');
        }
    });
}

// 上一题
function prevQuestion() {
    if (currentExamQuestionIndex > 0) {
        loadExamQuestion(currentExamQuestionIndex - 1);
    }
}

// 下一题
function nextQuestion() {
    if (currentExamQuestionIndex < examQuestions.length - 1) {
        loadExamQuestion(currentExamQuestionIndex + 1);
    }
}

// 更新控制按钮状态
function updateControlButtons() {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');
    
    if (prevBtn) {
        prevBtn.style.display = currentExamQuestionIndex > 0 ? 'flex' : 'none';
    }
    
    if (nextBtn) {
        nextBtn.style.display = currentExamQuestionIndex < examQuestions.length - 1 ? 'flex' : 'none';
    }
    
    if (submitBtn) {
        submitBtn.style.display = currentExamQuestionIndex === examQuestions.length - 1 ? 'flex' : 'none';
    }
}

// 开始考试计时
function startExamTimer() {
    examTimer = setInterval(() => {
        examTimeRemaining--;
        updateExamTimerDisplay();
        
        if (examTimeRemaining <= 0) {
            // 时间到，自动交卷
            stopExamTimer();
            showToast('考试时间到，系统自动交卷', 'warning');
            setTimeout(() => {
                submitExam();
            }, 2000);
        }
    }, 1000);
}

// 停止考试计时
function stopExamTimer() {
    if (examTimer) {
        clearInterval(examTimer);
        examTimer = null;
    }
}

// 更新考试计时器显示
function updateExamTimerDisplay() {
    const timerElement = document.getElementById('examTimer');
    if (timerElement) {
        const minutes = Math.floor(examTimeRemaining / 60);
        const seconds = examTimeRemaining % 60;
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // 时间不足5分钟时变红
        if (examTimeRemaining <= 300) {
            timerElement.style.color = '#f44336';
        } else {
            timerElement.style.color = '#666';
        }
    }
}

// 显示交卷确认对话框
function showSubmitDialog() {
    const answeredCount = examAnswers.filter(answer => answer !== null).length;
    const unansweredCount = examQuestions.length - answeredCount;
    
    // 更新统计信息
    const answeredSpan = document.getElementById('answeredCount');
    const unansweredSpan = document.getElementById('unansweredCount');
    
    if (answeredSpan) answeredSpan.textContent = answeredCount;
    if (unansweredSpan) unansweredSpan.textContent = unansweredCount;
    
    // 显示对话框
    const dialog = document.getElementById('submitDialog');
    if (dialog) {
        dialog.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

// 关闭交卷确认对话框
function closeSubmitDialog() {
    const dialog = document.getElementById('submitDialog');
    if (dialog) {
        dialog.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// 提交考试
function submitExam() {
    closeSubmitDialog();
    stopExamTimer();
    
    const loading = showLoading('正在评分...');
    
    setTimeout(() => {
        hideLoading();
        calculateExamResult();
    }, 3000);
}

// 计算考试结果
function calculateExamResult() {
    const correctCount = examAnswers.reduce((count, answer, index) => {
        return answer === examQuestions[index].correctAnswer ? count + 1 : count;
    }, 0);
    
    const totalQuestions = examQuestions.length;
    const score = Math.round((correctCount / totalQuestions) * 100);
    const passed = score >= 90;
    const wrongCount = totalQuestions - correctCount;
    const accuracyRate = Math.round((correctCount / totalQuestions) * 100);
    
    // 计算考试用时
    const examDuration = calculateExamDuration();
    
    // 保存考试记录
    saveExamRecord({
        subject: currentExamType === 'subject1' ? '科目一模拟考试' : '科目四模拟考试',
        time: new Date().toLocaleString(),
        score: score,
        passed: passed,
        duration: examDuration,
        correctCount: correctCount,
        wrongCount: wrongCount
    });
    
    // 显示结果
    showExamResult({
        score,
        passed,
        duration: examDuration,
        correctCount,
        wrongCount,
        accuracyRate
    });
}

// 计算考试用时
function calculateExamDuration() {
    if (!examStartTime) return '00:00';
    
    const endTime = new Date();
    const duration = Math.floor((endTime - examStartTime) / 1000);
    const minutes = Math.floor(duration / 60);
    const seconds = duration % 60;
    
    return `${minutes}分${seconds}秒`;
}

// 保存考试记录
function saveExamRecord(record) {
    const history = Storage.get('examHistory', []);
    history.unshift(record); // 添加到开头
    
    // 只保留最近10条记录
    if (history.length > 10) {
        history.splice(10);
    }
    
    Storage.set('examHistory', history);
}

// 显示考试结果
function showExamResult(result) {
    const examPage = document.getElementById('examPage');
    const examResult = document.getElementById('examResult');
    
    if (examPage) examPage.classList.add('hidden');
    if (examResult) examResult.classList.remove('hidden');
    
    // 更新结果显示
    const resultIcon = document.getElementById('resultIcon');
    const resultTitle = document.getElementById('resultTitle');
    const finalScore = document.getElementById('finalScore');
    const resultStatus = document.getElementById('resultStatus');
    
    if (resultIcon) {
        resultIcon.className = result.passed ? 'fas fa-check-circle' : 'fas fa-times-circle';
        resultIcon.style.color = result.passed ? '#4CAF50' : '#f44336';
    }
    
    if (resultTitle) {
        resultTitle.textContent = result.passed ? '恭喜通过！' : '很遗憾未通过';
    }
    
    if (finalScore) {
        finalScore.textContent = result.score;
        finalScore.className = result.passed ? 'score-number' : 'score-number failed';
    }
    
    if (resultStatus) {
        resultStatus.textContent = result.passed ? '合格' : '不合格';
        resultStatus.className = result.passed ? 'result-status' : 'result-status failed';
    }
    
    // 更新详细信息
    const examDuration = document.getElementById('examDuration');
    const correctCount = document.getElementById('correctCount');
    const wrongCount = document.getElementById('wrongCount');
    const accuracyRate = document.getElementById('accuracyRate');
    
    if (examDuration) examDuration.textContent = result.duration;
    if (correctCount) correctCount.textContent = `${result.correctCount}题`;
    if (wrongCount) wrongCount.textContent = `${result.wrongCount}题`;
    if (accuracyRate) accuracyRate.textContent = `${result.accuracyRate}%`;
    
    // 生成错题分析
    generateWrongQuestionAnalysis();
}

// 生成错题分析
function generateWrongQuestionAnalysis() {
    const wrongQuestions = document.getElementById('wrongQuestions');
    if (!wrongQuestions) return;
    
    wrongQuestions.innerHTML = '';
    
    const wrongItems = [];
    examAnswers.forEach((answer, index) => {
        if (answer !== examQuestions[index].correctAnswer) {
            wrongItems.push({
                question: examQuestions[index],
                userAnswer: answer,
                index: index + 1
            });
        }
    });
    
    if (wrongItems.length === 0) {
        wrongQuestions.innerHTML = '<p style="text-align: center; color: #4CAF50;">恭喜！没有错题</p>';
        return;
    }
    
    wrongItems.forEach(item => {
        const wrongItem = document.createElement('div');
        wrongItem.className = 'wrong-question-item';
        
        wrongItem.innerHTML = `
            <div class="wrong-question-header">
                <span class="wrong-question-num">第${item.index}题</span>
                <span class="wrong-question-type">${currentExamType === 'subject1' ? '科目一' : '科目四'}</span>
            </div>
            <div class="wrong-question-text">${item.question.question}</div>
            <div class="wrong-answer-info">
                您的答案: <span class="wrong-answer">${item.userAnswer || '未作答'}</span> | 
                正确答案: <span class="correct-answer">${item.question.correctAnswer}</span>
            </div>
        `;
        
        wrongQuestions.appendChild(wrongItem);
    });
}

// 查看答案解析
function viewAllAnswers() {
    showToast('答案解析功能开发中...', 'info');
}

// 重新考试
function retakeExam() {
    if (confirm('确定要重新开始考试吗？')) {
        startExam(currentExamType);
    }
}

// 显示历史记录
function showHistory() {
    showToast('考试历史功能开发中...', 'info');
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 监听交卷确认对话框点击事件
    const submitDialog = document.getElementById('submitDialog');
    if (submitDialog) {
        submitDialog.addEventListener('click', function(e) {
            if (e.target === submitDialog) {
                closeSubmitDialog();
            }
        });
    }
    
    // 监听考试页面的键盘事件
    document.addEventListener('keydown', function(e) {
        const examPage = document.getElementById('examPage');
        if (examPage && !examPage.classList.contains('hidden')) {
            switch(e.key) {
                case 'ArrowLeft':
                    prevQuestion();
                    break;
                case 'ArrowRight':
                    nextQuestion();
                    break;
                case 'Escape':
                    if (confirm('确定要退出考试吗？未保存的答案将丢失。')) {
                        stopExamTimer();
                        showExamSelection();
                    }
                    break;
            }
        }
    });
    
    // 防止页面刷新时丢失考试状态
    window.addEventListener('beforeunload', function(e) {
        const examPage = document.getElementById('examPage');
        if (examPage && !examPage.classList.contains('hidden')) {
            e.preventDefault();
            e.returnValue = '考试进行中，确定要离开吗？';
        }
    });
});