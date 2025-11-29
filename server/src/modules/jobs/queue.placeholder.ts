// Placeholder for job queue (e.g., BullMQ / RabbitMQ) for future scaling
export function enqueue(name: string, payload: any) {
  console.log('Enqueued job', name, payload);
}

