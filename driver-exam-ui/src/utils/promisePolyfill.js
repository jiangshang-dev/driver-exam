// Promise polyfill loader
// 简化版本，直接确保Promise可用

/**
 * 确保Promise可用（简化版本）
 * @returns {Promise<void>}
 */
export const ensurePromise = () => {
  // 如果Promise已经可用，直接返回resolved promise
  if (typeof Promise !== 'undefined') {
    return Promise.resolve();
  }

  // 如果没有Promise，尝试从ES6 Promise获取
  if (typeof window !== 'undefined') {
    // 直接在window对象上设置Promise
    if (typeof window.ES6Promise !== 'undefined') {
      if (typeof window.ES6Promise.Promise === 'function') {
        window.Promise = window.ES6Promise.Promise;
      } else {
        window.Promise = window.ES6Promise;
      }
      console.log('Applied ES6 Promise from window.ES6Promise');
      return Promise.resolve();
    }

    // 如果window.Promise存在但全局Promise不存在
    if (typeof window.Promise !== 'undefined') {
      Promise = window.Promise;
      console.log('Applied Promise from window.Promise');
      return Promise.resolve();
    }
  }

  // 如果都没有，创建一个基本的polyfill（仅用于避免错误）
  if (typeof window !== 'undefined' && typeof window.Promise === 'undefined') {
    window.Promise = function PromisePolyfill() {};
    console.log('Applied basic Promise polyfill');
  }

  return Promise.resolve();
};

/**
 * 检查Promise状态
 * @returns {Object} Promise状态信息
 */
export const getPromiseStatus = () => {
  return {
    available: typeof Promise !== 'undefined',
    loaded: true,
    loading: false
  };
};
