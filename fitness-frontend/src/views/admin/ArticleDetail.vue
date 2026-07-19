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
        </div>
        <div v-if="article.coverImage" class="article-cover">
          <img :src="getImageUrl(article.coverImage)" alt="cover" />
        </div>
      </div>

      <el-divider />

      <div class="article-content" v-html="formattedContent"></div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View } from '@element-plus/icons-vue'
import { getArticleDetail } from '@/api/article'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref(null)

const escapeHtml = (content) => {
  if (!content) return ''
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const normalizeContentImages = (content) => {
  return content.replace(/(<img[^>]+src=["'])(?!https?:\/\/)([^"']+)(["'][^>]*>)/gi, (_, prefix, src, suffix) => {
    return `${prefix}${getImageUrl(src)}${suffix}`
  })
}

const formattedContent = computed(() => {
  const content = article.value?.content || ''
  if (/<[a-z][\s\S]*>/i.test(content)) {
    return normalizeContentImages(content)
  }
  return escapeHtml(content).replace(/\r\n/g, '\n').replace(/\n/g, '<br>')
})

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

.article-cover {
  margin-top: 20px;
}

.article-cover img {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  border-radius: 12px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #333;
  min-height: 300px;
  margin-bottom: 20px;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
}
</style>
