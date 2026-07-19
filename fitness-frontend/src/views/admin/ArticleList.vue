<template>
  <div class="article-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健身科普</span>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="全部分类" clearable @change="loadData">
            <el-option label="运动知识" value="运动知识" />
            <el-option label="饮食建议" value="饮食建议" />
            <el-option label="康复指导" value="康复指导" />
            <el-option label="器械使用" value="器械使用" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索标题或内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- 文章列表 -->
      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="article in articleList" :key="article.articleId" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="article-card" shadow="hover" @click="viewDetail(article.articleId)">
            <img v-if="article.coverImage" :src="getImageUrl(article.coverImage)" class="cover-image" />
            <div v-else class="cover-placeholder">
              <el-icon :size="60"><Document /></el-icon>
            </div>
            <div class="article-content">
              <el-tag v-if="article.isTop === 1" type="danger" size="small" style="margin-bottom: 8px">置顶</el-tag>
              <h3>{{ article.title }}</h3>
              <el-tag size="small">{{ article.category }}</el-tag>
              <p class="author">作者：{{ article.authorName }}</p>
              <div class="stats">
                <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ article.likeCount }}</span>
                <span><el-icon><Collection /></el-icon> {{ article.collectCount }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[8, 16, 24]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, View, Star, Collection } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const loading = ref(false)
const articleList = ref([])

const searchForm = reactive({
  category: '',
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      category: searchForm.category || undefined,
      keyword: searchForm.keyword || undefined
    })
    articleList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (articleId) => {
  router.push(`/admin/article/${articleId}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.article-list {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.article-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  transform: translateY(-4px);
}

.cover-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-placeholder {
  width: 100%;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.article-content {
  padding: 12px 0;
}

.article-content h3 {
  margin: 8px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.author {
  color: #909399;
  font-size: 12px;
  margin: 8px 0;
}

.stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
