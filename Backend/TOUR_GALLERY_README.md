# Tour Gallery System

## Overview
The Tour Gallery system manages images and videos for the Dubai Discoveries mobile app. It provides a comprehensive API for fetching gallery content grouped by type (Image/Video) and category.

## Database Collection
- **Collection Name**: `inner_app_tour_gallary`
- **Model**: `TourGallery`

## Data Structure

### TourGallery Schema
```javascript
{
  title: String,           // Required: Title of the gallery item
  description: String,     // Optional: Description
  type: String,           // Required: 'image' or 'video'
  filePath: String,       // Required: File path in uploads directory
  fileName: String,       // Required: Original filename
  fileSize: Number,       // File size in bytes
  mimeType: String,       // MIME type (e.g., 'image/jpeg', 'video/mp4')
  dimensions: {           // Image/video dimensions
    width: Number,
    height: Number
  },
  duration: Number,       // Video duration in seconds
  thumbnail: String,      // Thumbnail path for videos
  category: String,       // 'hero', 'attractions', 'services', 'packages', 'general'
  tags: [String],         // Searchable tags
  isActive: Boolean,      // Whether item is active
  isFeatured: Boolean,    // Whether item is featured
  order: Number,          // Display order
  uploadDate: Date,       // Upload timestamp
  metadata: {             // Additional metadata
    originalName: String,
    uploadPath: String,
    processed: Boolean,
    compression: {
      quality: Number,
      format: String
    }
  }
}
```

## API Endpoints

### Base URL
```
/api/gallery
```

### 1. Get All Gallery Items
```
GET /api/gallery
```
Returns all active gallery items sorted by order and upload date.

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 15,
  "message": "Gallery items retrieved successfully"
}
```

### 2. Get Gallery Items Grouped by Type
```
GET /api/gallery/grouped-by-type
```
Returns gallery items grouped into images and videos.

**Response:**
```json
{
  "success": true,
  "data": {
    "images": [...],
    "videos": [...]
  },
  "summary": {
    "total": 15,
    "images": 10,
    "videos": 5
  },
  "message": "Gallery items grouped by type retrieved successfully"
}
```

### 3. Get Images Only
```
GET /api/gallery/images
```
Returns only image gallery items.

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 10,
  "message": "Images retrieved successfully"
}
```

### 4. Get Videos Only
```
GET /api/gallery/videos
```
Returns only video gallery items.

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 5,
  "message": "Videos retrieved successfully"
}
```

### 5. Get Hero Gallery Items
```
GET /api/gallery/hero
```
Returns hero category items (limited to 6).

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 3,
  "message": "Hero gallery items retrieved successfully"
}
```

### 6. Get Gallery Items by Category
```
GET /api/gallery/category/:category
```
Returns items for a specific category.

**Categories:**
- `hero` - Hero images/videos
- `attractions` - Attraction-related content
- `services` - Service-related content
- `packages` - Tour package content
- `general` - General content

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 5,
  "category": "attractions",
  "message": "Gallery items for category 'attractions' retrieved successfully"
}
```

### 7. Get Featured Gallery Items
```
GET /api/gallery/featured
```
Returns featured gallery items (limited to 10).

**Response:**
```json
{
  "success": true,
  "data": [...],
  "count": 5,
  "message": "Featured gallery items retrieved successfully"
}
```

## Seeded Data

The system comes with 15 pre-seeded gallery items:

### Images (10 items)
- **Hero Images (3)**: Burj Khalifa Sunset, Palm Jumeirah Aerial, Dubai Marina Night
- **Attraction Images (3)**: Museum of the Future, Dubai Miracle Garden, Wild Wadi Waterpark
- **Service Images (2)**: Luxury Hotel Suite, Desert Safari Camp
- **Package Images (2)**: Dubai City Tour, Honeymoon Package

### Videos (5 items)
- **General (1)**: Dubai City Overview (3 min)
- **Attractions (2)**: Burj Khalifa Experience (2 min), Dubai Fountain Show (1.5 min)
- **Services (2)**: Desert Safari Adventure (4 min), Luxury Hotel Tour (2.5 min)

## File Structure

```
uploads/
├── images/           # Image files
├── videos/           # Video files
└── thumbnails/       # Video thumbnails
```

## Usage Examples

### Mobile App Integration

#### 1. Load Hero Gallery
```javascript
// Fetch hero images for slider
const response = await fetch('/api/gallery/hero');
const heroItems = response.data;
```

#### 2. Load Images by Category
```javascript
// Fetch attraction images
const response = await fetch('/api/gallery/category/attractions');
const attractionImages = response.data.filter(item => item.type === 'image');
```

#### 3. Load Videos
```javascript
// Fetch all videos
const response = await fetch('/api/gallery/videos');
const videos = response.data;
```

#### 4. Grouped Gallery View
```javascript
// Fetch grouped data for gallery view
const response = await fetch('/api/gallery/grouped-by-type');
const { images, videos } = response.data;
```

## Testing

Run the test script to verify the API:
```bash
node test-tour-gallery-api.js
```

## Seeder

The tour gallery seeder runs automatically on server startup in development mode. To run manually:

```javascript
const seedTourGallery = require('./seedTourGallery');
await seedTourGallery();
```

## Features

- ✅ **Type Grouping**: Separate images and videos
- ✅ **Category Filtering**: Filter by hero, attractions, services, packages, general
- ✅ **Featured Items**: Highlight important content
- ✅ **Order Management**: Control display order
- ✅ **File Metadata**: Track file size, dimensions, duration
- ✅ **Thumbnail Support**: Video thumbnails
- ✅ **Tag System**: Searchable tags
- ✅ **Active/Inactive**: Toggle item visibility

## Future Enhancements

- [ ] **File Upload API**: Upload new gallery items
- [ ] **Image Processing**: Automatic thumbnail generation
- [ ] **Video Compression**: Optimize video files
- [ ] **CDN Integration**: Serve files from CDN
- [ ] **Search API**: Search by tags and titles
- [ ] **Pagination**: Handle large galleries
- [ ] **Caching**: Redis caching for performance 