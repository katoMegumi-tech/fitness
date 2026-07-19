<template>
  <div class="article-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的文章</span>
          <div>
            <el-radio-group v-model="filterStatus" @change="loadArticles" style="margin-right: 10px">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="DRAFT">草稿</el-radio-button>
              <el-radio-button label="PENDING">待审核</el-radio-button>
              <el-radio-button label="APPROVED">已通过</el-radio-button>
              <el-radio-button label="REJECTED">已拒绝</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="showCreateDialog">创建文章</el-button>
          </div>
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
        <el-table-column label="审核备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.auditRemark">{{ row.auditRemark }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.publishStatus !== 'PUBLISHED'" 
              size="small" 
              @click="editArticle(row)">
              编辑
            </el-button>
            <el-button 
              v-if="row.publishStatus === 'DRAFT' && row.auditStatus !== 'PENDING'" 
              size="small" 
              type="primary" 
              @click="publishArticle(row.articleId)">
              提交审核
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
          <div class="editor-shell">
            <div class="editor-toolbar">
              <el-button size="small" @click="formatArticle('bold')">B</el-button>
              <el-button size="small" @click="formatArticle('italic')">I</el-button>
              <el-button size="small" @click="formatArticle('underline')">U</el-button>
              <el-button size="small" @click="formatArticle('insertUnorderedList')">列表</el-button>
              <el-button size="small" @click="formatArticle('formatBlock', 'h2')">标题</el-button>
              <el-button size="small" @click="triggerInsertEditorImage">插入图片</el-button>
              <el-button size="small" @click="formatArticle('removeFormat')">清除格式</el-button>
            </div>
            <div
              ref="editorRef"
              class="article-editor"
              contenteditable="true"
              @input="handleEditorInput"
            ></div>
            <input
              ref="editorImageInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleEditorImageChange"
            >
          </div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyArticles, createArticle, updateArticle, publishArticle as publishArticleApi, deleteArticle as deleteArticleApi } from '@/api/article'
import { uploadSingle } from '@/api/upload'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const articleList = ref([])
const filterStatus = ref('')
const editorRef = ref(null)
const editorImageInputRef = ref(null)

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
const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getMyArticles({
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

// 兼容旧的方法名
const loadData = loadArticles

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  nextTick(() => {
    setEditorContent('')
  })
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
  nextTick(() => {
    setEditorContent(article.content || '')
  })
}

const setEditorContent = (content) => {
  if (!editorRef.value) return
  editorRef.value.innerHTML = content || ''
}

const handleEditorInput = () => {
  articleForm.content = editorRef.value?.innerHTML || ''
}

const focusEditor = () => {
  editorRef.value?.focus()
}

const formatArticle = (command, value = null) => {
  focusEditor()
  document.execCommand(command, false, value)
  handleEditorInput()
}

const triggerInsertEditorImage = () => {
  editorImageInputRef.value?.click()
}

const handleEditorImageChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await uploadSingle(file)
    focusEditor()
    document.execCommand('insertImage', false, getImageUrl(res.data))
    handleEditorInput()
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    if (editorImageInputRef.value) {
      editorImageInputRef.value.value = ''
    }
  }
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

// 发布文章（提交审核）
const publishArticle = async (articleId) => {
  try {
    await ElMessageBox.confirm('确定提交此文章到管理员审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await publishArticleApi(articleId)
    ElMessage.success('文章已提交审核')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
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
  setEditorContent('')
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

.editor-shell {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid var(--el-border-color);
}

.article-editor {
  min-height: 320px;
  padding: 12px;
  outline: none;
  line-height: 1.8;
  word-break: break-word;
}

.article-editor:empty::before {
  content: '请输入文章内容，可插入图片';
  color: #a8abb2;
}

.article-editor :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 8px 0;
}
</style>
