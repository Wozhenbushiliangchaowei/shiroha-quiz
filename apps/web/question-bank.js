// v25.2 起，内置题库改为按需加载，避免启动时同步加载完整题库。
window.questionBankIndex = [
  {
    id: 'c1-full',
    name: 'C1驾照科目一模拟练习题库',
    count: 393,
    file: 'data/c1-full.json',
    description: '内置示例题库，按需加载。'
  }
];
window.questionBank = {
  meta: {
    title: '内置题库（按需加载）',
    questionCount: 0,
    lazy: true,
    sourceNote: '完整内置题库已拆分为 data/c1-full.json，请在首页或题库管理中点击“加载内置题库”。'
  },
  questions: []
};
