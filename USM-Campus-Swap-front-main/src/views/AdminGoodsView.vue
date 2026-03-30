<template>
  <div class="admin-container">
    <div class="page-header">
      <h2><el-icon><Management /></el-icon> System Goods Management</h2>
      <el-tag type="danger" effect="dark">Admin Portal</el-tag>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="searchFilter">
        <el-form-item label="Product Name">
          <el-input v-model="searchFilter.title" placeholder="Search keywords..." />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="searchFilter.status" placeholder="All Status" clearable>
            <el-option label="Active" :value="1" />
            <el-option label="Banned/Off-shelf" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAdminGoods">Search</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="goodsList" border stripe style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="Cover" width="100">
        <template #default="scope">
          <el-image :src="scope.row.coverImage" style="width: 60px; height: 60px; border-radius: 4px" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="Title" show-overflow-tooltip />
      <el-table-column prop="price" label="Price (RM)" width="120" />
      <el-table-column prop="userName" label="Seller" width="150" />
      <el-table-column label="Status" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? 'Active' : 'Banned' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="scope">
          <el-button
              v-if="scope.row.status === 1"
              type="danger"
              size="small"
              @click="handleUpdateStatus(scope.row, 0)"
          >Ban Item</el-button>
          <el-button
              v-else
              type="success"
              size="small"
              @click="handleUpdateStatus(scope.row, 1)"
          >Restore</el-button>
          <el-button type="primary" size="small" link @click="viewDetail(scope.row.id)">View</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Management } from '@element-plus/icons-vue';
import myAxios from "@/plugins/request";

const router = useRouter();
const loading = ref(false);
const goodsList = ref([]);
const searchFilter = ref({ title: '', status: null });

// 加载所有商品 (管理员接口通常需要单独的分页逻辑)
const loadAdminGoods = async () => {
  loading.value = true;
  try {
    // 这里复用你的列表查询接口，但在后端如果是 Admin 角色，可以查询所有(包含已禁用)的商品
    const res = await myAxios.get('/goods/list/page/vo', { params: searchFilter.value });
    if (res && res.records) {
      goodsList.value = res.records;
    }
  } catch (e) {
    console.error('Failed to load goods:', e);
  } finally {
    loading.value = false;
  }
};

// 执行禁用或恢复操作
const handleUpdateStatus = (row, newStatus) => {
  const actionText = newStatus === 0 ? 'Ban' : 'Restore';
  ElMessageBox.confirm(`Are you sure you want to ${actionText} this item?`, 'Security Warning', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(async () => {
    try {
      // 关键：这里调用你刚刚整合好的 update 接口
      await myAxios.post('/goods/update', {
        id: row.id,
        status: newStatus
      });
      ElMessage.success(`Item has been ${actionText}ned`);
      loadAdminGoods(); // 刷新列表
    } catch (e) {
      console.error(e);
    }
  });
};

const viewDetail = (id) => router.push(`/goods/${id}`);

onMounted(() => loadAdminGoods());
</script>

<style scoped>
.admin-container { padding: 20px; background-color: #f0f2f5; min-height: 100vh; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
</style>