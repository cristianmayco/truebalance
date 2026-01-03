import { useEffect } from 'react';

interface SEOProps {
  title: string;
  description?: string;
  keywords?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImage?: string;
  ogType?: 'website' | 'article';
  twitterCard?: 'summary' | 'summary_large_image';
  canonical?: string;
  noindex?: boolean;
  nofollow?: boolean;
}

/**
 * SEO Component
 *
 * Dynamically updates SEO-related meta tags for a page.
 * Handles document title, meta descriptions, Open Graph, Twitter Cards, etc.
 *
 * @example
 * ```tsx
 * function BillsPage() {
 *   return (
 *     <>
 *       <SEO
 *         title="Minhas Contas"
 *         description="Gerencie todas as suas contas e parcelas em um só lugar"
 *         keywords="contas, finanças, parcelas, gerenciamento"
 *       />
 *       <div>Page content...</div>
 *     </>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * function ReportsPage() {
 *   return (
 *     <>
 *       <SEO
 *         title="Relatórios Financeiros"
 *         description="Visualize gráficos e relatórios detalhados dos seus gastos"
 *         ogImage="https://truebalance.app/og-reports.png"
 *         ogType="article"
 *       />
 *       <div>Page content...</div>
 *     </>
 *   );
 * }
 * ```
 */
export function SEO({
  title,
  description = 'Sistema completo de gerenciamento financeiro pessoal. Gerencie contas, cartões de crédito, faturas e visualize relatórios detalhados.',
  keywords = 'finanças pessoais, gerenciamento financeiro, contas, cartões de crédito, faturas, relatórios',
  ogTitle,
  ogDescription,
  ogImage = 'https://truebalance.app/og-image.png',
  ogType = 'website',
  twitterCard = 'summary_large_image',
  canonical,
  noindex = false,
  nofollow = false,
}: SEOProps) {
  useEffect(() => {
    // Set document title
    document.title = title ? `${title} | TrueBalance` : 'TrueBalance';

    // Helper function to set or update meta tag
    const setMetaTag = (name: string, content: string, attribute: 'name' | 'property' = 'name') => {
      let element = document.querySelector(`meta[${attribute}="${name}"]`);

      if (!element) {
        element = document.createElement('meta');
        element.setAttribute(attribute, name);
        document.head.appendChild(element);
      }

      element.setAttribute('content', content);
    };

    // Basic meta tags
    if (description) {
      setMetaTag('description', description);
    }

    if (keywords) {
      setMetaTag('keywords', keywords);
    }

    // Robots meta
    const robotsContent = [];
    if (noindex) robotsContent.push('noindex');
    if (nofollow) robotsContent.push('nofollow');
    if (!noindex && !nofollow) robotsContent.push('index', 'follow');

    setMetaTag('robots', robotsContent.join(', '));

    // Open Graph tags
    setMetaTag('og:title', ogTitle || title, 'property');
    setMetaTag('og:description', ogDescription || description, 'property');
    setMetaTag('og:image', ogImage, 'property');
    setMetaTag('og:type', ogType, 'property');
    setMetaTag('og:site_name', 'TrueBalance', 'property');

    // Twitter Card tags
    setMetaTag('twitter:card', twitterCard);
    setMetaTag('twitter:title', ogTitle || title);
    setMetaTag('twitter:description', ogDescription || description);
    setMetaTag('twitter:image', ogImage);

    // Canonical URL
    if (canonical) {
      let linkElement = document.querySelector('link[rel="canonical"]') as HTMLLinkElement;

      if (!linkElement) {
        linkElement = document.createElement('link');
        linkElement.setAttribute('rel', 'canonical');
        document.head.appendChild(linkElement);
      }

      linkElement.setAttribute('href', canonical);
    }
  }, [
    title,
    description,
    keywords,
    ogTitle,
    ogDescription,
    ogImage,
    ogType,
    twitterCard,
    canonical,
    noindex,
    nofollow,
  ]);

  // This component doesn't render anything
  return null;
}
