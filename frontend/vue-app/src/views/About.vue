<template>
  <div class="harmony-about">
    <div class="harmony-container">
      <!-- 页面标题 -->
      <div class="harmony-page-header">
        <h2 class="harmony-page-title">
          <span class="harmony-text-gradient">关于我们</span>
        </h2>
        <p class="harmony-page-subtitle">专业、高效、值得信赖的汽车保养服务平台</p>
      </div>

      <!-- 公司介绍 -->
      <div class="harmony-about-content">
        <div class="harmony-section">
          <div class="harmony-section-icon">🚗</div>
          <h3 class="harmony-section-title">平台介绍</h3>
          <p class="harmony-section-text">
            我们是一个专注于汽车保养预约服务的互联网平台，致力于为车主提供便捷、透明、高效的汽车保养服务。
            通过整合优质门店资源，建立标准化服务流程，让车主享受到更好的保养体验。
          </p>
        </div>

        <div class="harmony-section">
          <div class="harmony-section-icon">⭐</div>
          <h3 class="harmony-section-title">我们的优势</h3>
          <div class="harmony-features-grid">
            <div class="harmony-feature-item">
              <div class="harmony-feature-icon">🏪</div>
              <h4>优质门店</h4>
              <p>严格筛选合作门店，确保服务质量</p>
            </div>
            <div class="harmony-feature-item">
              <div class="harmony-feature-icon">💰</div>
              <h4>价格透明</h4>
              <p>明码标价，无隐藏消费</p>
            </div>
            <div class="harmony-feature-item">
              <div class="harmony-feature-icon">⚡</div>
              <h4>快速预约</h4>
              <p>在线预约，节省等待时间</p>
            </div>
            <div class="harmony-feature-item">
              <div class="harmony-feature-icon">🛡️</div>
              <h4>服务保障</h4>
              <p>完善的售后服务体系</p>
            </div>
          </div>
        </div>

        <div class="harmony-section">
          <div class="harmony-section-icon">📞</div>
          <h3 class="harmony-section-title">联系我们</h3>
          <div class="harmony-contact-info">
            <p><strong>客服热线：</strong>400-888-8888</p>
            <p><strong>工作时间：</strong>周一至周日 8:00-20:00</p>
            <p><strong>电子邮箱：</strong>service@carmaintenance.com</p>
            <p><strong>公司地址：</strong>北京市朝阳区建国路88号</p>
          </div>
        </div>

        <div class="harmony-stats">
          <div class="harmony-stat-item">
            <div class="harmony-stat-number">1000+</div>
            <div class="harmony-stat-label">合作门店</div>
          </div>
          <div class="harmony-stat-item">
            <div class="harmony-stat-number">50W+</div>
            <div class="harmony-stat-label">服务车主</div>
          </div>
          <div class="harmony-stat-item">
            <div class="harmony-stat-number">4.8</div>
            <div class="harmony-stat-label">用户评分</div>
          </div>
          <div class="harmony-stat-item">
            <div class="harmony-stat-number">98%</div>
            <div class="harmony-stat-label">满意度</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'About'
}
</script>

<style scoped>
.harmony-about {
  min-height: calc(100vh - 200px);
  padding: var(--harmony-space-xl) 0;
}

.harmony-page-header {
  text-align: center;
  margin-bottom: var(--harmony-space-2xl);
}

.harmony-page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-md);
}

.harmony-page-subtitle {
  font-size: 1.1rem;
  color: var(--harmony-text-secondary);
}

.harmony-about-content {
  max-width: 1000px;
  margin: 0 auto;
}

.harmony-section {
  background: var(--harmony-glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--harmony-border);
  border-radius: var(--harmony-radius-lg);
  padding: var(--harmony-space-2xl);
  margin-bottom: var(--harmony-space-xl);
}

.harmony-section-icon {
  font-size: 3rem;
  margin-bottom: var(--harmony-space-lg);
}

.harmony-section-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-lg);
}

.harmony-section-text {
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--harmony-text-secondary);
}

.harmony-features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--harmony-space-xl);
  margin-top: var(--harmony-space-xl);
}

.harmony-feature-item {
  text-align: center;
}

.harmony-feature-icon {
  font-size: 3rem;
  margin-bottom: var(--harmony-space-md);
}

.harmony-feature-item h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--harmony-text-primary);
  margin-bottom: var(--harmony-space-sm);
}

.harmony-feature-item p {
  color: var(--harmony-text-secondary);
  line-height: 1.6;
}

.harmony-contact-info {
  font-size: 1.1rem;
  line-height: 2;
  color: var(--harmony-text-secondary);
}

.harmony-contact-info strong {
  color: var(--harmony-text-primary);
}

.harmony-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--harmony-space-xl);
  margin-top: var(--harmony-space-2xl);
}

.harmony-stat-item {
  text-align: center;
  padding: var(--harmony-space-xl);
  background: var(--harmony-primary-gradient);
  border-radius: var(--harmony-radius-lg);
  color: white;
}

.harmony-stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--harmony-space-sm);
}

.harmony-stat-label {
  font-size: 1rem;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .harmony-page-title {
    font-size: 2rem;
  }

  .harmony-section {
    padding: var(--harmony-space-xl);
  }

  .harmony-features-grid {
    grid-template-columns: 1fr;
  }

  .harmony-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
