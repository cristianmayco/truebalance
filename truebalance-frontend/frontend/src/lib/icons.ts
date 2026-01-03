/**
 * Centralized Icon Imports
 *
 * Import all Lucide icons used in the application from this file.
 * This enables better tree-shaking and reduces bundle size.
 *
 * Instead of:
 * import { Home, User } from 'lucide-react' // ❌ Imports entire library
 *
 * Use:
 * import { HomeIcon, UserIcon } from '@/lib/icons' // ✅ Imports only needed icons
 */

// Layout & Navigation
export {
  Menu as MenuIcon,
  X as CloseIcon,
  Home as HomeIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
  ChevronDown as ChevronDownIcon,
  ChevronUp as ChevronUpIcon,
  MoreVertical as MoreVerticalIcon,
  MoreHorizontal as MoreHorizontalIcon,
} from 'lucide-react';

// Theme
export {
  Sun as SunIcon,
  Moon as MoonIcon,
} from 'lucide-react';

// Financial
export {
  DollarSign as DollarSignIcon,
  CreditCard as CreditCardIcon,
  Receipt as ReceiptIcon,
  Wallet as WalletIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  BarChart3 as BarChart3Icon,
  PieChart as PieChartIcon,
  LineChart as LineChartIcon,
} from 'lucide-react';

// Actions
export {
  Plus as PlusIcon,
  Minus as MinusIcon,
  Edit as EditIcon,
  Trash2 as TrashIcon,
  Save as SaveIcon,
  Check as CheckIcon,
  AlertCircle as AlertCircleIcon,
  AlertTriangle as AlertTriangleIcon,
  Info as InfoIcon,
  XCircle as XCircleIcon,
} from 'lucide-react';

// Data & Files
export {
  Calendar as CalendarIcon,
  Clock as ClockIcon,
  Download as DownloadIcon,
  Upload as UploadIcon,
  FileText as FileTextIcon,
  Filter as FilterIcon,
  Search as SearchIcon,
  RefreshCw as RefreshIcon,
} from 'lucide-react';

// UI Elements
export {
  Loader2 as LoaderIcon,
  Eye as EyeIcon,
  EyeOff as EyeOffIcon,
  Settings as SettingsIcon,
  User as UserIcon,
  Bell as BellIcon,
  Mail as MailIcon,
} from 'lucide-react';

// Arrows & Navigation
export {
  ArrowLeft as ArrowLeftIcon,
  ArrowRight as ArrowRightIcon,
  ArrowUp as ArrowUpIcon,
  ArrowDown as ArrowDownIcon,
  ExternalLink as ExternalLinkIcon,
} from 'lucide-react';

/**
 * Usage Examples:
 *
 * @example
 * ```tsx
 * import { HomeIcon, DollarSignIcon } from '@/lib/icons';
 *
 * function Dashboard() {
 *   return (
 *     <div>
 *       <HomeIcon className="w-5 h-5" />
 *       <DollarSignIcon className="w-6 h-6" />
 *     </div>
 *   );
 * }
 * ```
 */
