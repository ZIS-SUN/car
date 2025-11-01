<template>
  <footer class="harmony-footer">
    <div class="harmony-container">
      <div class="harmony-footer-content">
        <div class="harmony-footer-section">
          <div class="harmony-footer-logo">
            <div class="harmony-logo-icon">🚗</div>
            <h3 class="harmony-footer-title">汽车保养预约系统</h3>
          </div>
          <p class="harmony-footer-desc">专业的汽车保养预约平台，为您提供便捷、专业、透明的预约服务</p>
          <div class="harmony-footer-social">
            <div class="harmony-social-item">📱</div>
            <div class="harmony-social-item">💬</div>
            <div class="harmony-social-item">📧</div>
          </div>
        </div>
        
        <div class="harmony-footer-section">
          <h4 class="harmony-section-title">服务支持</h4>
          <ul class="harmony-footer-links">
            <li><a href="#" class="harmony-footer-link">帮助中心</a></li>
            <li><a href="#" class="harmony-footer-link">联系客服</a></li>
            <li><a href="#" class="harmony-footer-link">投诉建议</a></li>
            <li><a href="#" class="harmony-footer-link">服务条款</a></li>
            <li><a href="#" class="harmony-footer-link">隐私政策</a></li>
          </ul>
        </div>
        
        <div class="harmony-footer-section">
          <h4 class="harmony-section-title">快速导航</h4>
          <ul class="harmony-footer-links">
            <li><router-link to="/" class="harmony-footer-link">首页</router-link></li>
            <li><router-link to="/shops" class="harmony-footer-link">门店</router-link></li>
            <li><router-link to="/services" class="harmony-footer-link">服务</router-link></li>
            <li><router-link to="/about" class="harmony-footer-link">关于我们</router-link></li>
          </ul>
        </div>
        
        <div class="harmony-footer-section">
          <h4 class="harmony-section-title">联系我们</h4>
          <div class="harmony-contact-info">
            <div class="harmony-contact-item">
              <span class="harmony-contact-icon">📞</span>
              <span>400-888-8888</span>
            </div>
            <div class="harmony-contact-item">
              <span class="harmony-contact-icon">📧</span>
              <span>service@carmaintenance.com</span>
            </div>
            <div class="harmony-contact-item">
              <span class="harmony-contact-icon">📍</span>
              <span>北京市朝阳区科技园区</span>
            </div>
            <div class="harmony-contact-item">
              <span class="harmony-contact-icon">🕒</span>
              <span>7×24小时服务</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="harmony-footer-divider"></div>
      
      <div class="harmony-footer-bottom">
        <div class="harmony-copyright">
          <p>&copy; 2023 汽车保养预约系统. 保留所有权利.</p>
        </div>
        <div class="harmony-footer-badges">
          <span class="harmony-badge">安全认证</span>
          <span class="harmony-badge">服务保障</span>
          <span class="harmony-badge">正品保证</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter'
}
</script>

<style scoped>
.harmony-footer {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: white;
  padding: var(--harmony-space-2xl) 0 var(--harmony-space-lg);
  margin-top: auto;
  position: relative;
  overflow: hidden;
}

.harmony-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(0, 125, 255, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(102, 126, 234, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.harmony-footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: var(--harmony-space-2xl);
  margin-bottom: var(--harmony-space-xl);
  position: relative;
  z-index: 1;
}

/* Logo 区域 */
.harmony-footer-logo {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-md);
  margin-bottom: var(--harmony-space-lg);
}

.harmony-footer-logo .harmony-logo-icon {
  font-size: 2.5rem;
  background: var(--harmony-primary-gradient);
  border-radius: 50%;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--harmony-shadow-md);
}

.harmony-footer-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: white;
  margin: 0;
}

.harmony-footer-desc {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin-bottom: var(--harmony-space-lg);
}

/* 社交媒体 */
.harmony-footer-social {
  display: flex;
  gap: var(--harmony-space-md);
}

.harmony-social-item {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all var(--harmony-transition-base);
  backdrop-filter: blur(10px);
}

.harmony-social-item:hover {
  background: var(--harmony-primary);
  transform: translateY(-2px);
  box-shadow: var(--harmony-shadow-md);
}

/* 章节标题 */
.harmony-section-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: var(--harmony-space-lg);
  color: white;
  position: relative;
}

.harmony-section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 30px;
  height: 2px;
  background: var(--harmony-primary-gradient);
  border-radius: 1px;
}

/* 链接列表 */
.harmony-footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.harmony-footer-links li {
  margin-bottom: var(--harmony-space-sm);
}

.harmony-footer-link {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 0.95rem;
  transition: all var(--harmony-transition-base);
  display: inline-block;
  position: relative;
}

.harmony-footer-link:hover {
  color: white;
  transform: translateX(4px);
}

.harmony-footer-link::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: var(--harmony-primary);
  border-radius: 50%;
  opacity: 0;
  transition: opacity var(--harmony-transition-base);
}

.harmony-footer-link:hover::before {
  opacity: 1;
}

/* 联系信息 */
.harmony-contact-info {
  display: flex;
  flex-direction: column;
  gap: var(--harmony-space-md);
}

.harmony-contact-item {
  display: flex;
  align-items: center;
  gap: var(--harmony-space-sm);
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.95rem;
}

.harmony-contact-icon {
  font-size: 1.1rem;
  width: 24px;
  text-align: center;
}

/* 分割线 */
.harmony-footer-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
  margin: var(--harmony-space-xl) 0;
}

/* 底部区域 */
.harmony-footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.harmony-copyright {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

.harmony-footer-badges {
  display: flex;
  gap: var(--harmony-space-md);
}

.harmony-badge {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--harmony-radius-full);
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  transition: all var(--harmony-transition-base);
}

.harmony-badge:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .harmony-footer-content {
    grid-template-columns: 1fr 1fr;
    gap: var(--harmony-space-xl);
  }
}

@media (max-width: 768px) {
  .harmony-footer-content {
    grid-template-columns: 1fr;
    gap: var(--harmony-space-xl);
    text-align: center;
  }
  
  .harmony-footer-logo {
    justify-content: center;
  }
  
  .harmony-footer-social {
    justify-content: center;
  }
  
  .harmony-section-title::after {
    left: 50%;
    transform: translateX(-50%);
  }
  
  .harmony-footer-bottom {
    flex-direction: column;
    gap: var(--harmony-space-md);
    text-align: center;
  }
  
  .harmony-footer-badges {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .harmony-footer {
    padding: var(--harmony-space-xl) 0 var(--harmony-space-md);
  }
  
  .harmony-footer-content {
    gap: var(--harmony-space-lg);
  }
  
  .harmony-footer-logo .harmony-logo-icon {
    width: 48px;
    height: 48px;
    font-size: 2rem;
  }
  
  .harmony-footer-title {
    font-size: 1.1rem;
  }
  
  .harmony-contact-info {
    gap: var(--harmony-space-sm);
  }
  
  .harmony-contact-item {
    font-size: 0.9rem;
  }
}

/* 动画效果 */
.harmony-footer-section {
  animation: harmony-slideUp 0.8s ease-out;
}

.harmony-footer-section:nth-child(1) { animation-delay: 0.1s; }
.harmony-footer-section:nth-child(2) { animation-delay: 0.2s; }
.harmony-footer-section:nth-child(3) { animation-delay: 0.3s; }
.harmony-footer-section:nth-child(4) { animation-delay: 0.4s; }
</style>