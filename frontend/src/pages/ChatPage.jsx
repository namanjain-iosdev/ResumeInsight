import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import AppLayout from '../components/layout/AppLayout';
import ChatBubble from '../components/chat/ChatBubble';
import ChatInput from '../components/chat/ChatInput';
import { chatService } from '../services/chatService';

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [sending, setSending] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    chatService
      .getHistory()
      .then((res) => {
        const items = res?.data?.content ?? res?.content ?? [];
        setMessages(items);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (text) => {
    setSending(true);
    const userMsg = { message: text, isUser: true, timestamp: new Date().toISOString() };
    setMessages((m) => [...m, userMsg]);
    try {
      const res = await chatService.sendMessage(text);
      const reply = res?.data?.response ?? res?.response ?? '...';
      setMessages((m) => [...m, { message: reply, isUser: false, timestamp: new Date().toISOString() }]);
    } catch (err) {
      toast.error(err?.message || 'Chat failed');
    } finally {
      setSending(false);
    }
  };

  return (
    <AppLayout>
    <div className="max-w-3xl mx-auto p-6 flex flex-col h-[calc(100vh-8rem)]">
      <h1 className="text-2xl font-semibold text-white mb-4">Career Chat</h1>
      <div className="flex-1 overflow-y-auto space-y-4 pr-2">
        {messages.map((m, i) => (
          <ChatBubble key={i} message={m.message} isUser={m.isUser} timestamp={m.timestamp} />
        ))}
        <div ref={endRef} />
      </div>
      <div className="mt-4">
        <ChatInput onSend={handleSend} disabled={sending} />
      </div>
    </div>
    </AppLayout>
  );
};

export default ChatPage;
