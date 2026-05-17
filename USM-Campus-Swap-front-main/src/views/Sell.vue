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
              placeholder="Enter a clear and descriptive title (e.g. Calculus Textbook)"
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
                placeholder="Describe your item's features, flaws, or why you're selling it..."
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

        <div class="form-row" v-if="specificOptions && specificOptions.length > 0">
          <el-form-item :label="specificLabel" prop="specifics" class="specific-item">
            <el-select v-model="form.specifics" :placeholder="'Select ' + specificLabel" size="large" style="width: 100%">
              <el-option v-for="opt in specificOptions" :key="opt" :label="opt" :value="opt" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="Condition" prop="condition" style="margin-top: 20px;">
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
        <p class="section-subtitle">Clear photos help sell faster (up to 8 images)</p>
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
                Formats: JPG/PNG. Max size: 10MB per image.
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
            <el-option label="Main Campus (Penang)" value="Main Campus" />
            <el-option label="Engineering Campus (Nibong Tebal)" value="Engineering Campus" />
            <el-option label="Health Campus (Kelantan)" value="Health Campus" />
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
                <span>Delivery (Post)</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
            v-if="form.deliveryMethod === 1"
            label="Specific Meetup Landmark (For Map Location)"
            prop="address"
            class="pickup-location"
        >
          <el-select
              v-model="form.address"
              :placeholder="form.campus ? 'Select a famous landmark in ' + form.campus : 'Select campus first'"
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
          <p class="input-tip" v-if="form.address && form.address !== 'Other'">
            ✨ Choosing a landmark allows buyers to see the exact location on the map.
          </p>
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
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
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

// 🌟 新增：数据加载锁，防止 watch 误清空数据
const isDataLoading = ref(false)

const form = reactive({
  id: null, // 🌟 新增：必须带有 id 才能执行更新操作
  title: '',
  description: '',
  price: null,
  categoryId: null,
  specifics: '', 
  condition: 1,
  campus: '', 
  deliveryMethod: 1,
  address: '', 
})

// 分类逻辑
const currentCategoryName = computed(() => {
  const cat = categories.value.find(c => String(c.value) === String(form.categoryId))
  return cat ? String(cat.label).toLowerCase() : ''
})

const specificLabel = computed(() => {
  const name = currentCategoryName.value
  if (!name) return ''
  if (name === 'textbooks') return 'School / Major'
  if (name === 'electronics') return 'Device Type'
  if (name === 'transportation') return 'Vehicle Type'
  if (name === 'daily supplies') return 'Item Category'
  if (name === 'clothing') return 'Size / Type'
  if (name === 'sports equipment') return 'Sport Type'
  if (name === 'furniture') return 'Furniture Type'
  return ''
})

const specificOptions = computed(() => {
  const label = specificLabel.value
  if (label === 'School / Major') return ['Computer Science', 'Management', 'Engineering', 'Medical/Dental', 'Languages/Arts', 'General Education', 'Other']
  if (label === 'Device Type') return ['Phone', 'Laptop/PC', 'Tablet/iPad', 'Audio/Headphones', 'Computer Accessories', 'Other']
  if (label === 'Vehicle Type') return ['Bicycle', 'Motorcycle / e-Bike', 'Helmet/Accessories', 'Skateboard/Scooter', 'Other']
  if (label === 'Item Category') return ['Kitchenware', 'Cleaning Supplies', 'Toiletries/Bath', 'Storage/Organizers', 'Other']
  if (label === 'Size / Type') return ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'Free Size', 'Shoe: US 6-8', 'Shoe: US 9-11+']
  if (label === 'Sport Type') return ['Badminton', 'Basketball', 'Football/Futsal', 'Tennis/Ping Pong', 'Gym/Fitness', 'Other']
  if (label === 'Furniture Type') return ['Chair/Stool', 'Table/Desk', 'Bed/Mattress', 'Cabinet/Rack', 'Other']
  return []
})

// 🌟 修复：只有在非加载状态下，切换分类才会清空具体的选项
watch(() => form.categoryId, () => {
  if (!isDataLoading.value) {
    form.specifics = ''
  }
})

// 精准校内名胜地点数据
// 🌟 升级版：精准校内名胜地点数据 (Google Maps 优化版)
// 确保完全覆盖了旧的 const meetupData = { ... }
const meetupData = {
  'Main Campus': [
    'Dewan Utama Pelajar (DUP)', 
    'International Mobility & Collaboration Centre (IMCC)', 
    'Institute of Postgraduate Studies (IPS)', 
    'Dewan Kuliah Foyer (DK Foyer)',  
    'Pusat Sukan USM'
  ],
  'Engineering Campus': [
    
    'Desasiswa Lembaran', 
    'Desasiswa Jaya', 
    'Kompleks Pendidikan'
  ],
  'Health Campus': [
    'Hospital Universiti Sains Malaysia (HUSM)', 
    'Desasiswa Murni', 
    'Perpustakaan Kampus Kesihatan',
  ],
  'Other': ['Discuss in chat']
}

const currentMeetupLocations = computed(() => {
  return meetupData[form.campus] || []
})

// 🌟 修复：只有在非加载状态下，切换校区才会清空详细地址
const onCampusChange = () => {
  if (!isDataLoading.value) {
    form.address = ''
  }
}

// 初始化与加载
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
  isDataLoading.value = true // 🔒 开启数据加载锁
  
  try {
    if (categories.value.length === 0) {
      await loadCategories()
    }

    const res = await myAxios.get(`/goods/get/${id}`)
    if (res) {
      // 基础字段赋值
      form.id = res.id // 🌟 非常重要，将 id 绑定回 form
      form.title = res.title
      form.description = res.description
      form.price = res.price
      form.categoryId = res.categoryId
      form.condition = res.status === 2 ? 3 : (res.condition || 1)
      form.campus = res.campus || ''
      form.deliveryMethod = res.deliveryMethod || 1
      
      // 🌟 等待 vue 渲染出具体的 select 下拉框选项后再进行赋值，避免因找不到选项而显示空
      await nextTick()
      form.specifics = res.specifics || '' 
      form.address = res.address || ''
      
      // 图片处理...
      if (res.images) {
        let imageUrls = []
        try {
          imageUrls = typeof res.images === 'string' ? JSON.parse(res.images) : res.images
        } catch (e) { imageUrls = [res.images] }
        fileList.value = imageUrls.map((url, index) => ({
          name: `image-${index}`,
          url: url,
          status: 'success'
        }))
      }
    }
  } catch (error) {
    ElMessage.error('Failed to load item details')
  } finally {
    // 延迟 300 毫秒解除锁定，确保所有的 watch 和计算属性都稳定了
    setTimeout(() => {
      isDataLoading.value = false
    }, 300)
    pageLoading.value = false
  }
}

// AI 生成
const isGenerating = ref(false)
const handleAIGenerate = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('Please enter an Item Title first!')
    return
  }
  isGenerating.value = true
  try {
    const conditionMap = { 1: 'New', 2: 'Like New', 3: 'Good', 4: 'Fair' }
    let promptTitle = form.title
    if (form.specifics) promptTitle += ` (${specificLabel.value}: ${form.specifics})`

    const res = await myAxios.get('/ai/generate', {
      params: { title: promptTitle, condition: conditionMap[form.condition] || 'Used' }
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

// 上传逻辑
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
  } catch (error) { onError(error); }
};

// 验证规则
const rules = reactive({
  title: [{ required: true, message: 'Please enter a title', trigger: 'blur' }],
  description: [{ required: true, message: 'Please provide a description', trigger: 'blur' }],
  price: [{ required: true, message: 'Please set a price', trigger: 'blur' }],
  categoryId: [{ required: true, message: 'Select a category', trigger: 'change' }],
  campus: [{ required: true, message: 'Select your campus', trigger: 'change' }],
  specifics: [{ 
    validator: (rule, value, callback) => {
      if (specificOptions.value.length > 0 && !value) {
        callback(new Error(`Please select the ${specificLabel.value.toLowerCase()}`))
      } else { callback() }
    }, 
    trigger: 'change' 
  }],
  address: [{ 
    validator: (rule, value, callback) => {
      if (form.deliveryMethod === 1 && !value) {
        callback(new Error('Please select a meetup landmark for map positioning'))
      } else { callback() }
    }, 
    trigger: 'change' 
  }]
})

// 提交
const submitForm = async () => {
  if (!formRef.value) return
  try {
    loading.value = true
    await formRef.value.validate()
    const finalImages = fileList.value.map(f => f.url).filter(u => u && !u.startsWith('blob:'))
    if (form.deliveryMethod === 2) form.address = ''
    
    // 🌟 将 form 对象展开，这会带上正确的 id
    const requestData = {
      ...form,
      images: finalImages,
      coverImage: finalImages[0] || '',
      id: form.id || route.query.id // 优先使用 form 中绑定的 ID
    }
    
    const apiUrl = isEdit.value ? '/goods/update' : '/goods/publish'
    const res = await myAxios.post(apiUrl, requestData)
    if (res) {
      ElMessage.success(isEdit.value ? 'Updated!' : 'Published!')
      router.push('/my-products')
    }
  } catch (error) {
    ElMessage.error('Please check all required fields')
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
.sell-form { background: white; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.form-section { margin-bottom: 40px; padding-bottom: 30px; border-bottom: 1px solid #f0f0f0; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.desc-input-container { display: flex; flex-direction: column; gap: 12px; }
.ai-btn { align-self: flex-end; font-weight: 600; }
.delivery-option { display: flex; align-items: center; gap: 8px; }
.form-actions { text-align: center; margin-top: 40px; }
.publish-btn { width: 220px; height: 50px; font-size: 16px; font-weight: 700; border-radius: 25px; }
.input-tip { font-size: 12px; color: #67c23a; margin-top: 8px; display: flex; align-items: center; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 10px; display: flex; align-items: center; gap: 4px; }
</style>