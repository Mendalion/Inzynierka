export interface MessageDTO { id: string; sender: string; body: string; sentAt: string; }
export interface ConversationDTO { id: string; platform: string; unreadCount: number; lastMessageAt?: string; }

