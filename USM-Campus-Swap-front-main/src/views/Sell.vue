<template>
  <div class="sell-page">
    <div class="page-header">
      <h1>{{ isEdit ? 'Edit Your Item' : 'Publish Your Item' }}</h1>
      <p>{{ isEdit ? 'Update the details of your listed item' : 'Fill in the details about your item to start publish' }}</p>
    </div>

    <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        label-position="top"
        class="sell-form"
        v-loading="pageLoading"
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
          <div class="desc-input-container">
            <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                placeholder="Describe your item's condition, features, and any relevant details"
                maxlength="500"
                show-word-limit
            />
            <el-button 
                type="primary" 
                plain 
                class="ai-btn" 
                :loading="isGenerating" 
                @click="handleAIGenerate"
            >
              <el-icon><MagicStick /></el-icon>
              ✨ AI 帮我写 (Auto Generate)
            </el-button>
          </div>
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
        <h2><span style="color: #f56c6c; margin-right: 4px;">*</span>Photos</h2>
        <p class="section-subtitle">Add clear photos of your item (up to 8 images)</p>
        <el-form-item prop="images">
          <el-upload
              v-model:file-list="fileList"
              action="#"
              list-type="picture-card"
              :http-request="customUpload"
              :before-upload="beforeUpload"
              :limit="8"
              accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">
                <el-icon><InfoFilled /></el-icon>
                Supported formats: JPG, JPEG, PNG. Max size: 10MB per image.
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </div>

      <div class="form-section">
        <h2>Location & Delivery</h2>
        
        <el-form-item label="Campus" prop="campus">
          <el-select
              v-model="form.campus"
              placeholder="Select your campus"
              size="large"
              style="width: 100%"
              @change="onCampusChange"
          >
            <el-option label="Main Campus" value="Main Campus" />
            <el-option label="Engineering Campus" value="Engineering Campus" />
            <el-option label="Health Campus" value="Health Campus" />
            <el-option label="Other" value="Other" />
          </el-select>
        </el-form-item>

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
            label="Specific Meetup Location"
            prop="address"
            class="pickup-location"
        >
          <el-select
              v-model="form.address"
              :placeholder="form.campus ? 'Select a location in ' + form.campus : 'Please select campus first'"
              size="large"
              style="width: 100%"
              :disabled="!form.campus"
          >
            <el-option
                v-for="loc in currentMeetupLocations"
                :key="loc"
                :label="loc"
                :value="loc"
            />
            <el-option label="Other (Discuss in chat)" value="Other" />
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
          {{ isEdit ? 'Update Item' : 'Publish Item' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Location, Box, Promotion, InfoFilled, MagicStick } from '@element-plus/icons-vue'
import myAxios from "@/plugins/request.js"

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const pageLoading = ref(false)

const isEdit = ref(false)
const categories = ref([])
const fileList = ref([])

const form = reactive({
  title: '',
  description: '',
  price: null,
  categoryId: null,
  condition: 1,
  campus: '', 
  deliveryMethod: 1,
  address: '', 
})

// 🌟 地点数据配置表
const meetupData = {
  'Main Campus': ['Student Center', 'Library', 'RST Cafe', 'DK Foyer', 'Pusat Sukan'],
  'Engineering Campus': ['Desasiswa Jaya', 'Library', 'Engineering Cafe', 'Main Gate'],
  'Health Campus': ['Mydin Kubang Kerian', 'Library', 'Hospital USM (HUSM)', 'Cafeteria'],
  'Other': ['Discuss with seller']
}

// 🌟 计算属性：根据当前选中的校区，返回对应的地点数组
const currentMeetupLocations = computed(() => {
  return meetupData[form.campus] || []
})

// 🌟 当校区改变时，清空之前选的具体地点，避免跨校区数据混乱
const onCampusChange = () => {
  form.address = ''
}

onMounted(async () => {
  await loadCategories()
  const productId = route.query.id
  if (productId) {
    isEdit.value = true
    loadProductDetail(productId)
  }
})

const loadProductDetail = async (id) => {
  pageLoading.value = true
  try {
    const res = await myAxios.get(`/goods/get/${id}`)
    if (res) {
      form.title = res.title
      form.description = res.description
      form.price = res.price
      form.categoryId = res.categoryId
      form.condition = res.status === 2 ? 3 : (res.condition || 1)
      form.campus = res.campus || ''
      form.deliveryMethod = res.deliveryMethod || 1
      form.address = res.address || ''
      
      if (res.images) {
        let imageUrls = []
        try {
          imageUrls = typeof res.images === 'string' ? JSON.parse(res.images) : res.images
        } catch (e) {
          imageUrls = [res.images]
        }
        if (Array.isArray(imageUrls)) {
          fileList.value = imageUrls.map((url, index) => ({
            name: `image-${index}`,
            url: url,
            status: 'success'
          }))
        }
      }
    }
  } catch (error) {
    ElMessage.error('Failed to load item details')
  } finally {
    pageLoading.value = false
  }
}

// AI, Upload 等逻辑保持不变...
const isGenerating = ref(false)
const handleAIGenerate = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('Please enter an Item Title first!')
    return
  }
  isGenerating.value = true
  try {
    const conditionMap = { 1: 'New', 2: 'Like New', 3: 'Good', 4: 'Fair' }
    const res = await myAxios.get('/ai/generate', {
      params: { title: form.title, condition: conditionMap[form.condition] || 'Used' }
    })
    if (res) {
      form.description = typeof res === 'string' ? res : (res.data || res)
      ElMessage.success('AI description generated!')
    }
  } catch (error) {
    ElMessage.error('AI generation failed')
  } finally {
    isGenerating.value = false
  }
}

const beforeUpload = (file) => {
  const isValidFormat = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type);
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isValidFormat) ElMessage.error('Only JPG/PNG supported');
  if (!isLt10M) ElMessage.error('Max 10MB');
  return isValidFormat && isLt10M;
}

const customUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append('file', file);
  try {
    const realUrl = await myAxios.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    if (realUrl) {
      const index = fileList.value.findIndex(f => f.uid === file.uid);
      if (index !== -1) {
        fileList.value[index] = { uid: file.uid, url: realUrl, status: 'success' };
        fileList.value = [...fileList.value];
      }
      onSuccess(realUrl);
    }
  } catch (error) {
    onError(error);
  }
};

const rules = reactive({
  title: [{ required: true, message: 'Required', trigger: 'blur' }],
  description: [{ required: true, message: 'Required', trigger: 'blur' }],
  price: [{ required: true, message: 'Required', trigger: 'blur' }],
  categoryId: [{ required: true, message: 'Required', trigger: 'change' }],
  campus: [{ required: true, message: 'Please select your campus', trigger: 'change' }],
  address: [{ 
    validator: (rule, value, callback) => {
      if (form.deliveryMethod === 1 && !value) {
        callback(new Error('Please select the meetup location'))
      } else {
        callback()
      }
    }, 
    trigger: 'change' 
  }]
})

const submitForm = async () => {
  if (!formRef.value) return
  try {
    loading.value = true
    await formRef.value.validate()
    const finalImages = fileList.value.map(f => f.url).filter(u => u && !u.startsWith('blob:'))
    if (form.deliveryMethod === 2) form.address = ''
    
    const requestData = {
      ...form,
      images: finalImages,
      coverImage: finalImages[0] || '',
      id: route.query.id 
    }
    const apiUrl = isEdit.value ? '/goods/update' : '/goods/publish'
    const res = await myAxios.post(apiUrl, requestData)
    if (res) {
      ElMessage.success(isEdit.value ? 'Updated!' : 'Published!')
      router.push('/my-products')
    }
  } catch (error) {
    ElMessage.error('Please check your input')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await myAxios.get('/category/list')
  if (res) categories.value = res.map(c => ({ label: c.name, value: c.id }))
}
</script>

<style scoped>
.sell-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { text-align: center; margin-bottom: 40px; }
.sell-form { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.form-section { margin-bottom: 40px; padding-bottom: 30px; border-bottom: 1px solid #eee; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.desc-input-container { display: flex; flex-direction: column; gap: 12px; }
.ai-btn { align-self: flex-end; }
.delivery-option { display: flex; align-items: center; gap: 8px; }
.form-actions { text-align: center; margin-top: 40px; }
.publish-btn { width: 200px; height: 48px; }
</style>