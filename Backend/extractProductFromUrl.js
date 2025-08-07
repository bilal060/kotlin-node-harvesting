
async function extractProductFromUrl(url) {
  try {
    console.log('⚡ FAST AI-powered product extraction from URL:', url);
    console.time('⏱️ Total Extraction Time');

    // Step 1: FAST Replicate AI extraction (8-second timeout)
    if (this.isConfigured && this.replicate) {
      try {
        console.log('🤖 Using Replicate AI for product extraction...');
        console.time('⏱️ AI Response Time');

        const aiPromise = this.replicate.run(
          "meta/llama-2-13b-chat", // Switched to smaller, faster model
          {
            input: {
              prompt: `Analyze this product URL and extract product information in JSON format. Return ONLY valid JSON, no other text.
              
              URL: ${url}
              Expected JSON format:
              {
                "name": "Product name from URL",
                "brand": "Brand name",
                "category": "Product category",
                "price": "Price if available",
                "currency": "USD",
                "description": "Brief product description",
                "store_domain": "Store domain from URL",
                "image_url": "Product main image URL if visible"
              }`
            }
          }
        );

        const timeoutPromise = this.timeoutReject(8000, 'AI timeout');
        const aiResult = await Promise.race([aiPromise, timeoutPromise]);

        console.timeEnd('⏱️ AI Response Time');
        console.log('🤖 Replicate AI response:', aiResult);

        // Parse AI response
        let productInfo;
        try {
          const jsonMatch = aiResult.match(/\{[\s\S]*\}/);
          if (jsonMatch) {
            productInfo = JSON.parse(jsonMatch[0]);
            console.log('✅ AI response parsed successfully:', productInfo);
          } else {
            console.log('⚠️ No JSON found in AI response:', aiResult);
            throw new Error('No JSON found in AI response');
          }
        } catch (parseError) {
          console.log('⚠️ Failed to parse AI response:', parseError.message);
          throw new Error('AI response parsing failed');
        }

        // Validate and clean AI data
        const cleanProductInfo = {
          name: productInfo.name || this.extractNameFromUrl(url),
          brand: productInfo.brand || this.extractBrandFromUrl(url),
          category: productInfo.category || 'General',
          price: parseFloat(productInfo.price) || 0,
          currency: productInfo.currency || 'USD',
          description: productInfo.description || `Product from ${url}`,
          store_domain: productInfo.store_domain || new URL(url).hostname.replace('www.', ''),
          product_url: url,
          image_url: productInfo.image_url || null,
          ai_enhanced: true,
          source: 'replicate_ai'
        };

        // Step 2: Generate CLIP embedding
        try {
          console.log('🔮 Generating CLIP embedding...');
          const textForEmbedding = `${cleanProductInfo.name} ${cleanProductInfo.brand} ${cleanProductInfo.category}`;
          const embedding = await this.generateTextEmbeddingWithTimeout(textForEmbedding, 5000);

          if (embedding) {
            cleanProductInfo.embedding = embedding;

            // Step 3: Store in Pinecone
            try {
              const productId = `product-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
              await this.storeProductEmbeddingWithTimeout(productId, cleanProductInfo, 3000);
              console.log('✅ Product embedding stored in Pinecone');
            } catch (pineconeError) {
              console.log('⚠️ Pinecone storage failed:', pineconeError.message);
            }
          }
        } catch (clipError) {
          console.log('⚠️ CLIP embedding failed:', clipError.message);
        }

        console.timeEnd('⏱️ Total Extraction Time');
        console.log('✅ FAST AI extraction completed:', cleanProductInfo.name);
        return cleanProductInfo;

      } catch (aiError) {
        console.log('⚠️ Replicate AI failed, using fast fallback:', aiError.message);
      }
    }

    // Step 4: FAST fallback - URL parsing only
    console.log('⚡ Using fast URL parsing fallback...');
    return this.fastUrlParsing(url);

  } catch (error) {
    console.error('❌ Product extraction failed:', error.message);
    return this.fastUrlParsing(url);
  }
}

// ⏱️ Helper function for timeout rejection
function timeoutReject(ms, message) {
  return new Promise((_, reject) => setTimeout(() => reject(new Error(message)), ms));
}
