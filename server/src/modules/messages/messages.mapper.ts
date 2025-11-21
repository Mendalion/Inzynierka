export function toConversationDTO(c: any) {
  return { id: c.id, platform: c.platform, unreadCount: c.unreadCount, lastMessageAt: c.lastMessageAt?.toISOString() };
}
export function toMessageDTO(m: any) {
  return { id: m.id, sender: m.sender, body: m.body, sentAt: m.sentAt.toISOString() };
}

