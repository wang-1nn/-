<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '@/net'
import {useAuthStore} from "@/stores/counter.js";
const authStore = useAuthStore()
const userId = authStore.user.userId
const props = defineProps({
  courseId: {
    type: Number,
    required: true
  },
  chapterId: {
    type: Number,
    default: null
  },
  contentId: {
    type: Number,
    default: null
  },
  contentType: {
    type: String,
    default: 'DOCUMENT'
  }
})

const notes = ref([])
const loading = ref(false)
const noteContent = ref('')
const noteTime = ref('')
const editingNote = ref(null)

// 获取笔记列表
const fetchNotes = () => {
  loading.value = true
  
  const params = {
    studentId: userId, // 修改这里，将userId改为studentId
    courseId: props.courseId
  }
  
  if (props.chapterId) {
    params.chapterId = props.chapterId
  }
  
  get('/api/student/learning-notes', params, 
    (message, data) => {
      notes.value = data || []
      loading.value = false
    },
    (message) => {
      ElMessage.error(message || '获取笔记列表失败')
      loading.value = false
    }
  )
}

// 添加笔记
const addNote = () => {
  if (!noteContent.value.trim()) {
    ElMessage.warning('笔记内容不能为空')
    return
  }
  
  const note = {
    studentId: userId,
    courseId: props.courseId,
    chapterId: props.chapterId,
    contentId: props.contentId,
    noteContent: noteContent.value,
    timePoint: props.contentType === 'VIDEO' ? noteTime.value : null
  }
  
  post('/api/student/learning-notes/add', note,
    (message) => {
      ElMessage.success(message || '添加笔记成功')
      noteContent.value = ''
      noteTime.value = ''
      fetchNotes()
    },
    (message) => {
      ElMessage.error(message || '添加笔记失败')
    }
  )
}

// 编辑笔记
const editNote = (note) => {
  editingNote.value = { ...note }
  noteContent.value = note.noteContent
  noteTime.value = note.noteTime || ''
}

// 更新笔记
const updateNote = () => {
  if (!noteContent.value.trim()) {
    ElMessage.warning('笔记内容不能为空')
    return
  }
  
  if (!editingNote.value) return
  
  const note = {
    studentId: userId,
    noteContent: noteContent.value,
    timePoint: props.contentType === 'VIDEO' ? noteTime.value : null
  }
  
  post(`/api/student/learning-notes/update/${editingNote.value.id}`, note,
      (message)=> {
        ElMessage.success(message || '更新笔记成功')
        noteContent.value = ''
        noteTime.value = ''
        editingNote.value = null
        fetchNotes()
      },
    (message) => {
      ElMessage.success(message || '更新笔记成功')
      noteContent.value = ''
      noteTime.value = ''
      editingNote.value = null
      fetchNotes()
    },
    (message) => {
      ElMessage.error(message || '更新笔记失败')
    }
  )
}

// 删除笔记
const deleteNote = (noteId) => {
  ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    post(`/api/student/learning-notes/delete/${noteId}`,
      { studentId: userId}, // 修改这里，将userId改为studentId
        (message)=> {
          ElMessage.success(message || '删除笔记成功')
          fetchNotes()
        },
      (message) => {
        ElMessage.success(message || '删除笔记成功')
        fetchNotes()
      },
      (message) => {
        ElMessage.error(message || '删除笔记失败')
      }
    )
  }).catch(() => {})
}

// 取消编辑
const cancelEdit = () => {
  editingNote.value = null
  noteContent.value = ''
  noteTime.value = ''
}

// 格式化时间
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 监听属性变化
watch(() => [props.courseId, props.chapterId], () => {
  fetchNotes()
}, { immediate: true })

onMounted(() => {
  fetchNotes()
})
</script>

<template>
  <div class="learning-notes-component">
    <div class="notes-header">
      <h3 class="text-lg font-medium">学习笔记</h3>
    </div>
    
    <!-- 添加/编辑笔记表单 -->
    <div class="notes-form bg-gray-50 p-4 rounded-lg mb-4">
      <div v-if="contentType === 'VIDEO'" class="mb-2">
        <label class="block text-sm text-gray-600 mb-1">时间点</label>
        <input 
          v-model="noteTime" 
          type="text" 
          placeholder="例如: 00:05:30" 
          class="w-full p-2 border rounded-lg text-sm"
        />
      </div>
      
      <div class="mb-2">
        <label class="block text-sm text-gray-600 mb-1">笔记内容</label>
        <textarea 
          v-model="noteContent" 
          rows="3" 
          placeholder="在这里添加你的笔记..." 
          class="w-full p-2 border rounded-lg text-sm"
        ></textarea>
      </div>
      
      <div class="flex justify-end space-x-2">
        <button 
          v-if="editingNote" 
          @click="cancelEdit" 
          class="px-3 py-1 border rounded-lg text-sm hover:bg-gray-100"
        >
          取消
        </button>
        <button 
          v-if="editingNote" 
          @click="updateNote" 
          class="px-3 py-1 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
        >
          更新笔记
        </button>
        <button 
          v-else 
          @click="addNote" 
          class="px-3 py-1 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
        >
          添加笔记
        </button>
      </div>
    </div>
    
    <!-- 笔记列表 -->
    <div class="notes-list space-y-3" v-loading="loading">
      <div v-if="notes.length === 0" class="text-center py-8 text-gray-400">
        <i class="fas fa-book-open text-2xl mb-2"></i>
        <p>暂无笔记</p>
      </div>
      
      <div v-for="note in notes" :key="note.id" class="note-item bg-white p-4 rounded-lg shadow-sm border border-gray-100">
        <div class="flex justify-between items-start">
          <div class="note-content">
            <div v-if="note.noteTime" class="text-xs text-gray-500 mb-1">
              <i class="fas fa-clock mr-1"></i> {{ note.noteTime }}
            </div>
            <div class="text-gray-800">{{ note.noteContent }}</div>
          </div>
          
          <div class="note-actions flex space-x-2">
            <button @click="editNote(note)" class="text-blue-600 hover:text-blue-800">
              <i class="fas fa-edit"></i>
            </button>
            <button @click="deleteNote(note.id)" class="text-red-600 hover:text-red-800">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
        
        <div class="note-meta mt-2 text-xs text-gray-500">
          <span>{{ formatDate(note.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.learning-notes-component {
  padding: 1rem;
}

.note-item {
  transition: all 0.2s ease;
}

.note-item:hover {
  border-color: #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
</style> 