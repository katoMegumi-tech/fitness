<template>
  <div class="article-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章审核</span>
          <el-radio-group v-model="filterStatus" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待审核</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="REJECTED">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 文章列表 -->
      <el-table :data="articleList" v-loading="loading" stripe>
        <el-table-column prop="articleNo" label="文章编号" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column label="发布状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.publishStatus === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.publishStatus === 'PUBLISHED'" type="success">已发布</el-tag>
            <el-tag v-else type="warning">已下线</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewArticle(row)">查看</el-button>
            <el-button 
              v-if="row.auditStatus === 'PENDING'" 
              size="small" 
              type="success" 
              @click="handleAudit(row, 'APPROVED')">
              通过
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'PENDING'" 
              size="small" 
              type="danger" 
              @click="handleAudit(row, 'REJECTED')">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 文章详情对话框 -->
    <el-dialog v-model="detailVisible" title="文章详情" width="800px">
      <div v-if="currentArticle" class="article-detail">
        <h2>{{ currentArticle.title }}</h2>
        <div class="article-meta">
          <span>作者：{{ currentArticle.authorName }}</span>
          <span>分类：{{ currentArticle.category }}</span>
          <span>发布时间：{{ formatTime(currentArticle.publishTime) }}</span>
        </div>
        <div v-if="currentArticle.coverImage" class="article-cover">
          <img :src="currentArticle.coverImage" alt="封面" />
        </div>
        <div class="article-content" v-html="currentArticle.content"></div>
        <div v-if="currentArticle.tags" class="article-tags">
          <el-tag v-for="tag in currentArticle.tags.split(',')" :key="tag" style="margin-right: 8px">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditType === 'APPROVED' ? '通过审核' : '拒绝审核'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="文章标题">
          <span>{{ currentArticle?.title }}</span>
        </el-form-item>
        <el-form-item label="审核备注" v-if="auditType === 'REJECTED'" required>
          <el-input 
            v-model="auditRemark" 
            type="textarea" 
            :rows="4"
            placeholder="请输入拒绝原因" />
        </el-form-item>
        <el-form-item label="审核备注" v-else>
          <el-input 
            v-model="auditRemark" 
            type="textarea" 
            :rows="4"
            placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllArticles, auditArticle } from '@/api/article'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const submitting = ref(false)
const detailVisible = ref(false)
const auditVisible = ref(false)
const articleList = ref([])
const currentArticle = ref(null)
const filterStatus = ref('')
const auditType = ref('')
const auditRemark = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllArticles({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      auditStatus: filterStatus.value || undefined
    })
    articleList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 查看文章
const viewArticle = (article) => {
  currentArticle.value = {
    ...article,
    coverImage: article.coverImage ? getImageUrl(article.coverImage) : null
  }
  detailVisible.value = true
}

// 处理审核
const handleAudit = (article, type) => {
  currentArticle.value = article
  auditType.value = type
  auditRemark.value = ''
  auditVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (auditType.value === 'REJECTED' && !auditRemark.value) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  submitting.value = true
  try {
    await auditArticle(currentArticle.value.articleId, auditType.value, auditRemark.value)
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.article-audit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.article-detail h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  color: #2c3e50;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
  color: #7f8c8d;
  font-size: 14px;
}

.article-cover {
  margin-bottom: 20px;
}

.article-cover img {
  max-width: 100%;
  border-radius: 8px;
}

.article-content {
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 20px;
}

.article-tags {
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}
</style>
