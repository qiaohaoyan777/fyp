<template>
  <div class="sell-page">
    <div class="page-header">
      <h1>Publish Your Item</h1>
      <p>Fill in the details about your item to start publish</p>
    </div>

    <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        label-position="top"
        class="sell-form"
    >
      <div class="form-section">
        <h2>Basic Information</h2>
        <el-form-item label="Item Title" prop="title">
          <el-input
              v-model="form.title"
              placeholder="Enter a clear and descriptive title"
              size="large"
              maxlength="60"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="Describe your item's condition, features, and any relevant details"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <h2>Price & Category</h2>
        <div class="form-row">
          <el-form-item label="Price (RM)" prop="price" class="price-item">
            <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                :step="0.01"
                size="large"
                controls-position="right"
                placeholder="0.00"
            />
          </el-form-item>

          <el-form-item label="Category" prop="categoryId" class="category-item">
            <el-select
                v-model="form.categoryId"
                placeholder="Select category"
                size="large"
                style="width: 100%"
            >
              <el-option
                  v-for="cat in categories"
                  :key="cat.value"
                  :label="cat.label"
                  :value="cat.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="Condition" prop="condition">
          <el-radio-group v-model="form.condition">
            <el-radio :label="1" border>New</el-radio>
            <el-radio :label="2" border>Like New</el-radio>
            <el-radio :label="3" border>Good</el-radio>
            <el-radio :label="4" border>Fair</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>

      <div class="form-section">
        <h2>Photos</h2>
        <p class="section-subtitle">Add clear photos of your item (up to 8 images)</p>
        <el-form-item prop="images">
          <el-upload
              :key="fileList.length"
              v-model:file-list="fileList"
              action="#"
              list-type="picture-card"
              :http-request="customUpload"
              :limit="8"
              accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </div>

      <div class="form-section">
        <h2>Delivery Method</h2>
        <el-form-item label="Delivery Option" prop="deliveryMethod">
          <el-radio-group v-model="form.deliveryMethod">
            <el-radio :label="1" border>
              <div class="delivery-option">
                <el-icon><Location /></el-icon>
                <span>Self Pickup</span>
              </div>
            </el-radio>
            <el-radio :label="2" border>
              <div class="delivery-option">
                <el-icon><Box /></el-icon>
                <span>Delivery</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
            v-if="form.deliveryMethod === 1"
            label="Pickup Location"
            prop="pickupLocation"
            class="pickup-location"
        >
          <el-select
              v-model="form.pickupLocation"
              placeholder="Select pickup location"
              size="large"
              style="width: 100%"
          >
            <el-option label="Main Campus - Student Center" value="Main Campus - Student Center" />
            <el-option label="Main Campus - Library" value="Main Campus - Library" />
            <el-option label="Engineering Campus" value="Engineering Campus" />
            <el-option label="Health Campus" value="Health Campus" />
            <el-option label="Other" value="Other" />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-actions">
        <el-button
            type="primary"
            size="large"
            @click="submitForm"
            :loading="loading"
            class="publish-btn"
        >
          <el-icon><Promotion /></el-icon>
          Publish Item
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Location, Box, Promotion } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const router = useRouter()
const formRef = ref()
const loading = ref(false)

// ⭐ 1. 必须定义 categories 变量，否则 loadCategories 会报错
const categories = ref([])
const fileList = ref([])

const form = reactive({
  title: '',
  description: '',
  price: null,
  categoryId: null,
  condition: 1,
  deliveryMethod: 1,
  pickupLocation: '',
  contactTypes: [1],
})

const rules = reactive({
  title: [
    { required: true, message: 'Please enter item title', trigger: 'blur' },
    { min: 5, max: 60, message: 'Title should be 5-60 characters', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please enter item description', trigger: 'blur' },
    { min: 10, max: 500, message: 'Description should be 10-500 characters', trigger: 'blur' }
  ],
  price: [
    { required: true, message: 'Please enter item price', trigger: 'blur' },
    { type: 'number', min: 0.01, message: 'Price must be greater than 0', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: 'Please select category', trigger: 'change' }
  ],
  deliveryMethod: [
    { required: true, message: 'Please select delivery method', trigger: 'change' }
  ]
})

const loadCategories = async () => {
  try {
    const res = await myAxios.get('/category/list')
    // 处理可能的 BaseResponse 包装
    const data = res.code === 0 ? res.data : res
    if (Array.isArray(data)) {
      categories.value = data.map(cat => ({
        label: cat.name,
        value: cat.id
      }))
    }
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

onMounted(() => {
  loadCategories()
})

const customUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append('file', file);

  try {
    // 1. 发起请求。注意：不要在这里判断 res.code，因为拦截器已经帮你判断并剥离了
    // 2. 显式覆盖 Content-Type
    const realUrl = await myAxios.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    // 此时 realUrl 直接就是后端返回的那个字符串地址
    if (realUrl && typeof realUrl === 'string') {
      const index = fileList.value.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        fileList.value[index] = {
          uid: file.uid,
          name: file.name,
          url: realUrl, // 现在的 realUrl 就是 "https://..."
          status: 'success'
        };
        fileList.value = [...fileList.value];
      }
      onSuccess(realUrl);
      ElMessage.success('Upload success!');
    }
  } catch (error) {
    // 拦截器 Promise.reject 的错误会进到这里
    console.error('Upload catch:', error);
    onError(error);
    ElMessage.error(error.message || 'Upload failed');
  }
};

const submitForm = async () => {
  if (!formRef.value) return

  const isUploading = fileList.value.some(file => file.status === 'uploading')
  if (isUploading) {
    ElMessage.warning('Images are still uploading. Please wait...')
    return
  }

  try {
    loading.value = true
    await formRef.value.validate()

    // 提取真实 URL 并排除 blob 本地预览地址
    const finalImages = fileList.value
        .map(file => file.url)
        .filter(url => url && !url.startsWith('blob:'))

    if (finalImages.length === 0 && fileList.value.length > 0) {
      ElMessage.error("Image upload failed to sync. Please try again.")
      loading.value = false
      return
    }

    const requestData = {
      ...form,
      images: finalImages,
      coverImage: finalImages.length > 0 ? finalImages[0] : '',
      campus: 'Main Campus',
      pickupLocation: form.deliveryMethod === 1 ? form.pickupLocation : ''
    }

    const res = await myAxios.post('/goods/publish', requestData)
    if (res) {
      ElMessage.success('Item published successfully!')
      router.push('/')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || 'Please fill in all required fields')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.sell-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.sell-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e5e7eb;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.delivery-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-actions {
  text-align: center;
  margin-top: 40px;
}

.publish-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
}
</style>