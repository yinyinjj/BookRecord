/**
 * 图片URL处理工具
 * 用于处理第三方图片URL，绕过防盗链机制
 */

/**
 * 需要通过代理加载的域名列表（img9 可以直接访问，不需要代理）
 */
const PROXY_DOMAINS = [
  'img1.doubanio.com',
  'img2.doubanio.com',
  'img3.doubanio.com',
  'img4.doubanio.com',
  'img5.doubanio.com',
  'img6.doubanio.com',
  'img7.doubanio.com',
  'img8.doubanio.com',
  // img9 可以直接访问，不需要代理
  'book.douban.com'
]

/**
 * 获取图片URL（通过代理绕过防盗链）
 * 如果是豆瓣等有防盗链的域名，返回代理URL
 * 否则返回原始URL
 *
 * @param url 原始图片URL
 * @returns 处理后的图片URL
 */
export function getProxyImageUrl(url: string): string {
  if (!url) return ''

  try {
    const urlObj = new URL(url)
    const host = urlObj.hostname

    // 检查是否需要代理
    if (PROXY_DOMAINS.includes(host)) {
      // 使用后端代理接口
      return `/api/v1/image-proxy?url=${encodeURIComponent(url)}`
    }

    return url
  } catch {
    // URL解析失败，返回原始URL
    return url
  }
}

/**
 * 获取默认封面URL
 * 当书籍没有封面时使用
 */
export function getDefaultCover(): string {
  // 返回一个简单的SVG占位图（base64编码）
  return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjE2MCIgdmlld0JveD0iMCAwIDEyMCAxNjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyMCIgaGVpZ2h0PSIxNjAiIGZpbGw9IiNlZWVlZWUiLz48dGV4dCB4PSI2MCIgeT0iODAiIGZpbGw9IiM5OTk5OTkiIGZvbnQtc2l6ZT0iMTYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7mrKfluIjngrpzPC90ZXh0Pjwvc3ZnPg=='
}