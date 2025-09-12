<template>
    <view class="container">
        <!-- 顶部导航栏 -->
        <view class="header">
            <view class="header-content">
                <view class="header-title">
                    <text class="logo">{{ sub == 1 ? '科目一' : '科目四' }}刷题</text>
                    <text class="progress-text">{{ currentNo }} / {{ count }}</text>
                </view>
                <view class="header-right">
                    <text class="filter-icon" @click="showFilterModal = true">筛选</text>
                </view>
            </view>
        </view>

        <!-- 题型筛选栏 -->
        <view class="filter-section" v-if="!showFilterModal">
            <scroll-view scroll-x="true" class="filter-scroll">
                <view class="filter-tabs">
                    <view 
                        class="filter-tab" 
                        :class="{ active: selectedQuestionType === '' }"
                        @click="selectQuestionType('')"
                    >
                        全部题型
                    </view>
                    <view 
                        v-for="(type, index) in questionTypes" 
                        :key="index"
                        class="filter-tab" 
                        :class="{ active: selectedQuestionType === type.code }"
                        @click="selectQuestionType(type.code)"
                    >
                        {{ type.name }}
                    </view>
                </view>
            </scroll-view>
        </view>

        <!-- 题型筛选弹窗 -->
        <view v-if="showFilterModal" class="modal" @click="showFilterModal = false">
            <view class="modal-content" @click.stop>
                <view class="modal-header">
                    <text class="modal-title">选择题型</text>
                    <text class="close" @click="showFilterModal = false">&times;</text>
                </view>
                <view class="modal-body">
                    <view class="question-types-grid">
                        <view 
                            class="type-item" 
                            :class="{ active: selectedQuestionType === '' }"
                            @click="selectQuestionTypeFromModal('')"
                        >
                            <text class="type-icon">📚</text>
                            <text class="type-name">全部题型</text>
                        </view>
                        <view 
                            v-for="(type, index) in questionTypes" 
                            :key="index"
                            class="type-item" 
                            :class="{ active: selectedQuestionType === type.code }"
                            @click="selectQuestionTypeFromModal(type.code)"
                        >
                            <text class="type-icon">{{ type.icon }}</text>
                            <text class="type-name">{{ type.name }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <!-- 题目卡片 -->
        <view class="question-card">
            <view class="question-header">
                <text class="question-number">第 {{ currentNo }} 题</text>
                <text class="question-type" v-if="currentQuestionTypeName">{{ currentQuestionTypeName }}</text>
            </view>
            
            <view class="question-content">
                <text class="question-text">{{ questions[currentQuestionIndex]?.question }}</text>
                
                <view v-if="questions[currentQuestionIndex]?.url != ''" class="question-image">
                    <image 
                        mode="aspectFill"
                        class="question-img"
                        :src="questions[currentQuestionIndex]?.url"
                        @click="showImagePopup(currentQuestionIndex, questions[currentQuestionIndex]?.url)"
                    />
                </view>
                
                <view class="options-container">
                    <view 
                        v-for="(option, index) in validOptions" 
                        :key="index"
                        class="option-item"
                        :class="{ 
                            selected: userAnswers[currentQuestionIndex] === option.text,
                            correct: showAnswer && isCorrectOption(option.index),
                            incorrect: showAnswer && userAnswers[currentQuestionIndex] === option.text && !isCorrectOption(option.index)
                        }"
                        @click="selectOption(option.index)"
                    >
                        <view class="option-letter">{{ String.fromCharCode(65 + option.index) }}</view>
                        <text class="option-text">{{ option.text }}</text>
                        <view v-if="showAnswer && isCorrectOption(option.index)" class="option-icon correct-icon">✓</view>
                        <view v-if="showAnswer && userAnswers[currentQuestionIndex] === option.text && !isCorrectOption(option.index)" class="option-icon incorrect-icon">✗</view>
                    </view>
                </view>
            </view>
        </view>
        <!-- 答案解析卡片 -->
        <view v-if="showAnswer" class="analysis-card">
            <view class="analysis-header">
                <text class="analysis-title">答案解析</text>
                <view class="result-icon" :class="{ correct: isCorrectAnswer, incorrect: !isCorrectAnswer }">
                    <text v-if="isCorrectAnswer">✓</text>
                    <text v-else>✗</text>
                </view>
            </view>
            
            <view class="analysis-content">
                <view class="answer-comparison">
                    <view class="answer-item">
                        <text class="answer-label">你的答案:</text>
                        <text class="answer-value user-answer">{{ userAnswerLetter || '未选择' }}</text>
                    </view>
                    <view class="answer-item">
                        <text class="answer-label">正确答案:</text>
                        <text class="answer-value correct-answer">{{ correctAnswerLetter }}</text>
                    </view>
                </view>
                
                <view class="explanation" v-if="questions[currentQuestionIndex]?.explains">
                    <text class="explanation-title">题目解析:</text>
                    <text class="explanation-text">{{ questions[currentQuestionIndex].explains }}</text>
                </view>
            </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="action-buttons">
            <view class="btn-group">
                <button 
                    class="btn btn-outline" 
                    @click="prevQuestion" 
                    :disabled="currentQuestionIndex === 0"
                >
                    上一题
                </button>
                <button 
                    class="btn btn-primary" 
                    @click="nextQuestion"
                    :disabled="currentQuestionIndex === questions.length - 1"
                >
                    下一题
                </button>
            </view>
            <!-- <button class="btn btn-refresh" @click="getData2(sub)">换一组题</button> -->
        </view>
        
        <!-- 广告位 -->
        <!-- <view class="ad-view">
            <ad adpid="1478175623" @load="onload" @close="onclose" @error="onerror"></ad>
        </view> -->
    </view>
</template>
<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app';
import { ref, computed, watch } from 'vue';
import { http } from '@/utils/http'

// 假设问题和用户答案已经加载到questions和userAnswers中
const questions = ref([]); // 题目列表
const userAnswers = ref([]); // 用户的答案列表
const currentQuestionIndex = ref(0); // 当前题目索引
const showAnswer = ref(false); // 是否显示答案
const count = ref(0)
const sub = ref(0)

// 题型筛选相关
const showFilterModal = ref(false); // 是否显示筛选弹窗
const selectedQuestionType = ref(''); // 选中的题型代码
const questionTypes = ref([ // 题型列表
    { code: 'traffic_signs', name: '交通标志', icon: '🚦' },
    { code: 'traffic_rules', name: '交通法规', icon: '📋' },
    { code: 'safe_driving', name: '安全行车', icon: '🚗' },
    { code: 'emergency_handling', name: '紧急处置', icon: '🚨' },
    { code: 'vehicle_knowledge', name: '机动车构造', icon: '🔧' },
    { code: 'driving_skills', name: '驾驶技能', icon: '🎯' },
    { code: 'weather_driving', name: '恶劣天气驾驶', icon: '🌧️' },
    { code: 'penalty_regulations', name: '违法处罚', icon: '⚖️' }
]);
onLoad((options) => {
    sub.value = options.sub
    console.log('sub.value---', sub.value);
    getData(sub.value)
})

// 计算当前题型名称
const currentQuestionTypeName = computed(() => {
    if (!selectedQuestionType.value) return '全部题型';
    const type = questionTypes.value.find(t => t.code === selectedQuestionType.value);
    return type ? type.name : '全部题型';
});

const getData = async (subject: number, questionType: string = '') => {
    let url = '/drive/driveTopicList?subject=' + subject + '&testType=order';
    if (questionType) {
        url += '&questionType=' + questionType;
    }
    const res = await http({
        url: url,
        method: 'GET'
    })
    console.log('res-', res.data);
    questions.value = res.data; // 假设API返回的数据结构包含题目列表
    count.value = res.data.length
    userAnswers.value = []; // 重置用户答案
    currentQuestionIndex.value = 0; // 重置题目索引
    showAnswer.value = false; // 隐藏答案
}

const getData2 = async (subject: number) => {
    await getData(subject, selectedQuestionType.value);
}

// 题型筛选函数
const selectQuestionType = async (typeCode: string) => {
    selectedQuestionType.value = typeCode;
    await getData(sub.value, typeCode);
}

// 从弹窗选择题型
const selectQuestionTypeFromModal = async (typeCode: string) => {
    selectedQuestionType.value = typeCode;
    showFilterModal.value = false;
    await getData(sub.value, typeCode);
}

// 检查某个选项是否为正确答案
const isCorrectOption = (optionIndex: number) => {
    const correctOptionIndex = parseInt(questions.value[currentQuestionIndex.value].answer) - 1;
    return optionIndex === correctOptionIndex;
}

// 切换题目
const prevQuestion = () => {
    if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--;
        showAnswer.value = false;
    }
}
const nextQuestion = () => {
    if (currentQuestionIndex.value < questions.value.length - 1) {
        currentQuestionIndex.value++;
        showAnswer.value = false;
    }
}

// 选择选项
const selectOption = (optionIndex: number) => {
    const options = [
        questions.value[currentQuestionIndex.value].item1,
        questions.value[currentQuestionIndex.value].item2,
        questions.value[currentQuestionIndex.value].item3,
        questions.value[currentQuestionIndex.value].item4
    ];
    userAnswers.value[currentQuestionIndex.value] = options[optionIndex];
    showAnswer.value = true;
    console.log('userAnswers.value---', userAnswers.value);

}

// 检查答案是否正确
const isCorrectAnswer = computed(() => {
    const correctOptionIndex = parseInt(questions.value[currentQuestionIndex.value].answer) - 1;
    const options = [
        questions.value[currentQuestionIndex.value].item1,
        questions.value[currentQuestionIndex.value].item2,
        questions.value[currentQuestionIndex.value].item3,
        questions.value[currentQuestionIndex.value].item4
    ];
    return userAnswers.value[currentQuestionIndex.value] === options[correctOptionIndex];
})

// 获取当前题目的正确答案
const correctAnswer = computed(() => {
    const correctOptionIndex = parseInt(questions.value[currentQuestionIndex.value].answer) - 1;
    const options = [
        questions.value[currentQuestionIndex.value].item1,
        questions.value[currentQuestionIndex.value].item2,
        questions.value[currentQuestionIndex.value].item3,
        questions.value[currentQuestionIndex.value].item4
    ];
    return options[correctOptionIndex];
})

// 定义 options 计算属性
const options = computed(() => {
    if (questions.value.length > 0) {
        return [
            questions.value[currentQuestionIndex.value].item1,
            questions.value[currentQuestionIndex.value].item2,
            questions.value[currentQuestionIndex.value].item3,
            questions.value[currentQuestionIndex.value].item4
        ];
    }
    return [];
})

// 定义有效选项计算属性（过滤空选项）
const validOptions = computed(() => {
    if (questions.value.length > 0) {
        const allOptions = [
            questions.value[currentQuestionIndex.value].item1,
            questions.value[currentQuestionIndex.value].item2,
            questions.value[currentQuestionIndex.value].item3,
            questions.value[currentQuestionIndex.value].item4
        ];
        
        return allOptions
            .map((option, index) => ({ text: option, index }))
            .filter(option => option.text !== '' && option.text != null);
    }
    return [];
})

// 计算选项数量
const optionCount = computed(() => {
    if (questions.value.length > 0) {
        const currentQuestion = questions.value[currentQuestionIndex.value];
        return [
            currentQuestion.item1,
            currentQuestion.item2,
            currentQuestion.item3,
            currentQuestion.item4
        ].filter(option => option !== '').length;
    }
    return 0;
});

// 计算当前题目编号
const currentNo = computed(() => {
    return currentQuestionIndex.value + 1;
});

// 定义选项字母
const optionLetters = ['A', 'B', 'C', 'D'];

// 计算用户的答案字母
const userAnswerLetter = computed(() => {
    const userAnswer = userAnswers.value[currentQuestionIndex.value];
    const options = [
        questions.value[currentQuestionIndex.value].item1,
        questions.value[currentQuestionIndex.value].item2,
        questions.value[currentQuestionIndex.value].item3,
        questions.value[currentQuestionIndex.value].item4
    ];
    const userAnswerIndex = options.indexOf(userAnswer);
    return userAnswerIndex !== -1 ? optionLetters[userAnswerIndex] : '';
});

// 计算正确答案字母
const correctAnswerLetter = computed(() => {
    const correctOptionIndex = parseInt(questions.value[currentQuestionIndex.value].answer) - 1;
    return optionLetters[correctOptionIndex];
});

// 显示图片弹出层
const showImagePopup = (index, src: string) => {
    //调用预览图片的方法
    uni.previewImage({
        urls: [src],
        current:index
    })
}

// 监听题目变化，重置答案显示
watch(currentQuestionIndex, () => {
    showAnswer.value = false;
})

const onload = (e) => {
      console.log("onload");
}
const onclose = (ez) => {
      // console.log("onclose: " + ez.detail);
}
const onerror = (ex) => {
      // console.log("onerror: " + ex.detail.errCode + " message:: " + ex.detail.errMsg);
}
</script>
<style lang="scss" scoped>
/* 基础样式 */
.container {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 120px;
}

/* 顶部导航栏 */
.header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 0 15px;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
}

.header-title {
    display: flex;
    flex-direction: column;
}

.logo {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 2px;
}

.progress-text {
    font-size: 14px;
    opacity: 0.9;
}

.filter-icon {
    font-size: 14px;
    padding: 6px 12px;
    background: rgba(255,255,255,0.2);
    border-radius: 15px;
    cursor: pointer;
}

/* 题型筛选栏 */
.filter-section {
    margin-top: 40px;
    padding: 15px 0;
    background: white;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.filter-scroll {
    white-space: nowrap;
}

.filter-tabs {
    display: flex;
    padding: 0 20px;
    gap: 10px;
}

.filter-tab {
    flex-shrink: 0;
    padding: 8px 16px;
    background: #f8f9fa;
    border-radius: 20px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 1px solid transparent;
}

.filter-tab.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: #667eea;
}

/* 题目卡片 */
.question-card {
    margin: 90px 20px 20px;
    background: white;
    border-radius: 15px;
    box-shadow: 0 2px 15px rgba(0,0,0,0.08);
    overflow: hidden;
}

.question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.question-number {
    font-size: 16px;
    font-weight: bold;
}

.question-type {
    font-size: 12px;
    padding: 4px 8px;
    background: rgba(255,255,255,0.2);
    border-radius: 10px;
}

.question-content {
    padding: 20px;
}

.question-text {
    font-size: 16px;
    line-height: 1.6;
    color: #333;
    margin-bottom: 15px;
    display: block;
}

.question-image {
    margin: 15px 0;
    text-align: center;
}

.question-img {
    width: 200px;
    height: 150px;
    border-radius: 10px;
    cursor: pointer;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* 选项样式 */
.options-container {
    margin-top: 20px;
}

.option-item {
    display: flex;
    align-items: center;
    padding: 15px;
    margin-bottom: 10px;
    background: #f8f9fa;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid transparent;
    position: relative;
}

.option-item:hover {
    background: #e9ecef;
}

.option-item.selected {
    background: #e3f2fd;
    border-color: #2196F3;
}

.option-item.correct {
    background: #e8f5e8;
    border-color: #4CAF50;
}

.option-item.incorrect {
    background: #ffebee;
    border-color: #f44336;
}

.option-letter {
    width: 30px;
    height: 30px;
    background: #667eea;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    margin-right: 15px;
    flex-shrink: 0;
}

.option-item.correct .option-letter {
    background: #4CAF50;
}

.option-item.incorrect .option-letter {
    background: #f44336;
}

.option-text {
    flex: 1;
    font-size: 15px;
    line-height: 1.5;
    color: #333;
}

.option-icon {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    margin-left: 10px;
}

.correct-icon {
    background: #4CAF50;
    color: white;
}

.incorrect-icon {
    background: #f44336;
    color: white;
}

/* 答案解析卡片 */
.analysis-card {
    margin: 20px;
    background: white;
    border-radius: 15px;
    box-shadow: 0 2px 15px rgba(0,0,0,0.08);
    overflow: hidden;
}

.analysis-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #e9ecef;
}

.analysis-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
}

.result-icon {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: bold;
    color: white;
}

.result-icon.correct {
    background: #4CAF50;
}

.result-icon.incorrect {
    background: #f44336;
}

.analysis-content {
    padding: 20px;
}

.answer-comparison {
    margin-bottom: 20px;
}

.answer-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
}

.answer-item:last-child {
    border-bottom: none;
}

.answer-label {
    font-size: 14px;
    color: #666;
}

.answer-value {
    font-size: 16px;
    font-weight: bold;
}

.user-answer {
    color: #2196F3;
}

.correct-answer {
    color: #4CAF50;
}

.explanation {
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
}

.explanation-title {
    font-size: 14px;
    font-weight: bold;
    color: #333;
    margin-bottom: 8px;
    display: block;
}

.explanation-text {
    font-size: 14px;
    line-height: 1.6;
    color: #666;
    display: block;
}

/* 操作按钮 */
.action-buttons {
    padding: 0 20px 20px;
}

.btn-group {
    display: flex;
    gap: 15px;
    margin-bottom: 15px;
}

.btn {
    flex: 1;
    padding: 15px;
    border-radius: 10px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    border: none;
    text-align: center;
}

.btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.btn-outline {
    background: white;
    color: #667eea;
    border: 2px solid #667eea;
}

.btn-outline:hover:not(:disabled) {
    background: #667eea;
    color: white;
}

.btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.btn-primary:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-refresh {
    width: 100%;
    background: #4CAF50;
    color: white;
}

.btn-refresh:hover {
    background: #45a049;
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4);
}

/* 筛选弹窗 */
.modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    z-index: 2000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.modal-content {
    background: white;
    border-radius: 15px;
    width: 100%;
    max-width: 400px;
    max-height: 80vh;
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.modal-title {
    font-size: 18px;
    font-weight: bold;
}

.close {
    font-size: 24px;
    cursor: pointer;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
}

.modal-body {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;
}

.question-types-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
}

.type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 15px;
    background: #f8f9fa;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid transparent;
    text-align: center;
}

.type-item:hover {
    background: #e9ecef;
    transform: translateY(-2px);
}

.type-item.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: #667eea;
}

.type-icon {
    font-size: 24px;
    margin-bottom: 8px;
    display: block;
}

.type-name {
    font-size: 12px;
    font-weight: 500;
    display: block;
}

.ad-view {
    background-color: #FFFFFF;
    margin: 20px;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
</style>