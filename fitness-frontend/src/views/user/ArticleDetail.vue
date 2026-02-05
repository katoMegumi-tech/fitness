<template>
  <div class="article-detail" v-loading="loading">
    <el-card v-if="article">
      <template #header>
        <el-button @click="goBack" text><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      </template>

      <div class="article-header">
        <h1>{{ article.title }}</h1>
        <div class="article-meta">
          <el-tag>{{ article.category }}</el-tag>
          <span class="author">作者：{{ article.authorName }}</span>
          <span class="time">{{ formatTime(article.publishTime) }}</span>
        </div>
        <div class="article-stats">
          <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
          <span><el-icon><Star /></el-icon> {{ article.likeCount }}</span>
          <span><el-icon><Collection /></el-icon> {{ article.collectCount }}</span>
        </div>
      </div>

      <el-divider />

      <div class="article-content" v-html="article.content"></div>

      <el-divider />

      <div class="article-actions">
        <el-button 
          :type="article.isLiked ? 'primary' : 'default'" 
          @click="toggleLike"
          :icon="Star">
          {{ article.isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount }})
        </el-button>
        <el-button 
          :type="article.isCollected ? 'warning' : 'default'" 
          @click="toggleCollect"
          :icon="Collection">
          {{ article.isCollected ? '已收藏' : '收藏' }} ({{ article.collectCount }})
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Star, Collection } from '@element-plus/icons-vue'
import { getArticleDetail, likeArticle, unlikeArticle, collectArticle, uncollectArticle } from '@/api/article'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref(null)

// 加载文章详情
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch (error) {
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 切换点赞
const toggleLike = async () => {
  try {
    if (article.value.isLiked) {
      await unlikeArticle(article.value.articleId)
      article.value.isLiked = false
      article.value.likeCount--
      ElMessage.success('取消点赞')
    } else {
      await likeArticle(article.value.articleId)
      article.value.isLiked = true
      article.value.likeCount++
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 切换收藏
const toggleCollect = async () => {
  try {
    if (article.value.isCollected) {
      await uncollectArticle(article.value.articleId)
      article.value.isCollected = false
      article.value.collectCount--
      ElMessage.success('取消收藏')
    } else {
      await collectArticle(article.value.articleId)
      article.value.isCollected = true
      article.value.collectCount++
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.article-detail {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.article-header h1 {
  margin: 0 0 15px 0;
  font-size: 28px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.article-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.article-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #333;
  min-height: 300px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.article-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>
