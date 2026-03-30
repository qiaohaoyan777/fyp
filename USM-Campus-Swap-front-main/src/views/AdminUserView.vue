<template>
  <div class="admin-container">
    <div class="page-header">
      <h2><el-icon><User /></el-icon> User Management</h2>
      <el-tag type="danger" effect="dark">System Administrator</el-tag>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="Username">
          <el-input v-model="query.username" placeholder="Search username..." clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">Search</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="userList" border stripe v-loading="loading" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="Avatar" width="100">
        <template #default="scope">
          <el-avatar :size="40" :src="scope.row.avatarUrl" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="Username" />
      <el-table-column prop="userAccount" label="Account" />

      <el-table-column prop="userRole" label="Role">
        <template #default="scope">
          <el-tag :type="scope.row.userRole === 1 ? 'danger' : 'success'">
            {{ scope.row.userRole === 1 ? 'Admin' : 'Student' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="Status" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.isDelete === 0 ? 'success' : 'danger'">
            {{ scope.row.isDelete === 0 ? 'Active' : 'Banned' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="Joined Date" width="180" />

      <el-table-column label="Actions" width="150" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.userRole !== 1">
            <el-button
                v-if="scope.row.isDelete === 0"
                type="danger"
                link
                @click="handleUpdateStatus(scope.row, 1)"
            >Ban User</el-button>
            <el-button
                v-else
                type="success"
                link
                @click="handleUpdateStatus(scope.row, 0)"
            >Restore</el-button>
          </template>
          <span v-else>--</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
          v-model:current-page="query.current"
          :page-size="query.size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { User, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import myAxios from "@/plugins/request"

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  username: ''
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/user/list/page', { params: query })
    if (res) {
      userList.value = res.records
      total.value = res.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.current = 1
  fetchUsers()
}

// ★★★ 修改状态更新逻辑 ★★★
const handleUpdateStatus = (row, newIsDelete) => {
  // newIsDelete: 1 = Ban, 0 = Restore
  const actionText = newIsDelete === 1 ? 'ban' : 'restore'

  ElMessageBox.confirm(
      `Are you sure you want to ${actionText} user: ${row.username}?`,
      'Security Warning',
      { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' }
  ).then(async () => {
    try {
      // 发送请求，后端会把 status 赋值给 isDelete
      await myAxios.post('/user/update/status', {
        id: row.id,
        status: newIsDelete
      })
      ElMessage.success(`User has been ${actionText}d successfully`)
      fetchUsers() // 刷新列表
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>