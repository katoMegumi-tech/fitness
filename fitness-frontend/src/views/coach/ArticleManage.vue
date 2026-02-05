<template>
  <div class="article-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的文章</span>
          <el-button type="primary" @click="showCreateDialog">创建文章</el-button>
        </div>
      </template>

      <!-- 文章列表 -->
      <el-table :data="articleList" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="状态" width="100">
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
        <el-table-column prop="collectCount" label="收藏" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editArticle(row)">编辑</el-button>
            <el-button 
              v-if="row.publishStatus === 'DRAFT'" 
              size="small" 
              type="primary" 
              @click="publishArticle(row.articleId)">
              发布
            </el-button>
            <el-button size="small" type="danger" @click="deleteArticle(row.articleId)">删除</el-button>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑文章' : '创建文章'" 
      width="800px"
      @close="resetForm">
      <el-form :model="articleForm" label-width="100px">
        <el-form-item label="文章标题" required>
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章分类" required>
          <el-select v-model="articleForm.category" placeholder="请选择分类">
            <el-option label="运动知识" value="运动知识" />
            <el-option label="饮食建议" value="饮食建议" />
            <el-option label="康复指导" value="康复指导" />
            <el-option label="器械使用" value="器械使用" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            action="#"
            :auto-upload="false"
            :on-change="handleCoverChange"
            :limit="1"
            list-type="picture">
            <el-button size="small">选择图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="articleForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="文章内容" required>
          <el-input 
            v-model="articleForm.content" 
            type="textarea" 
            :rows="15"
            placeholder="请输入文章内容（支持HTML）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitArticle" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyArticles, createArticle, updateArticle, publishArticle as publishArticleApi, deleteArticle as deleteArticleApi } from '@/api/article'
import { uploadSingle } from '@/api/upload'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const articleList = ref([])

const articleForm = reactive({
  articleId: null,
  title: '',
  category: '',
  content: '',
  coverImage: '',
  tags: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyArticles({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
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

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑文章
const editArticle = (article) => {
  isEdit.value = true
  articleForm.articleId = article.articleId
  articleForm.title = article.title
  articleForm.category = article.category
  articleForm.content = article.content
  articleForm.coverImage = article.coverImage
  articleForm.tags = article.tags
  dialogVisible.value = true
}

// 处理封面上传
const handleCoverChange = async (file) => {
  try {
    const res = await uploadSingle(file.raw)
    articleForm.coverImage = res.data
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
}

// 提交文章
const submitArticle = async () => {
  if (!articleForm.title || !articleForm.category || !articleForm.content) {
    ElMessage.warning('请填写必填项')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateArticle(articleForm.articleId, articleForm)
      ElMessage.success('文章更新成功')
    } else {
      await createArticle(articleForm)
      ElMessage.success('文章创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 发布文章
const publishArticle = async (articleId) => {
  try {
    await ElMessageBox.confirm('确定发布此文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await publishArticleApi(articleId)
    ElMessage.success('文章发布成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

// 删除文章
const deleteArticle = async (articleId) => {
  try {
    await ElMessageBox.confirm('确定删除此文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteArticleApi(articleId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  articleForm.articleId = null
  articleForm.title = ''
  articleForm.category = ''
  articleForm.content = ''
  articleForm.coverImage = ''
  articleForm.tags = ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.article-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
