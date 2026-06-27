import { BookOpen, ExternalLink } from 'lucide-react';

export default function ApiDocsButton() {
  const handleOpenDocs = () => {
    const backendUrl = `${window.location.protocol}//${window.location.hostname}:8080/swagger-ui/index.html`;
    
    window.open(backendUrl, '_blank', 'noopener,noreferrer');
  };

  return (
    <button
      onClick={handleOpenDocs}
      className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-slate-800 hover:bg-slate-700 active:bg-slate-900 rounded-lg shadow transition-colors duration-200"
      aria-label="View API Documentation via Swagger UI"
    >
      <BookOpen className="w-4 h-4" />
      <span>API Documentation</span>
      <ExternalLink className="w-3 h-3 opacity-60 ml-1" />
    </button>
  );
}