import { useMemo, useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function App() {
  const [channelId, setChannelId] = useState('');
  const [limit, setLimit] = useState(10);
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleFetch = async () => {
    if (!channelId.trim()) {
      setError('채널 ID를 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/shorts/rank?channelId=${encodeURIComponent(channelId)}&limit=${limit}`
      );
      if (!response.ok) {
        throw new Error('데이터를 불러오는 데 실패했습니다.');
      }
      const data = await response.json();
      setVideos(data);
    } catch (err) {
      setError(err.message || '오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const tableRows = useMemo(
    () =>
      videos.map((video, index) => (
        <tr key={video.videoId || index}>
          <td>{index + 1}</td>
          <td className="thumbnail">
            <a href={`https://www.youtube.com/watch?v=${video.videoId}`} target="_blank" rel="noreferrer">
              <img src={video.thumbnailUrl} alt={video.title} />
            </a>
          </td>
          <td>{video.title}</td>
          <td>{video.channelTitle}</td>
          <td>{Number(video.viewCount).toLocaleString('ko-KR')}</td>
          <td>{new Date(video.publishedAt).toLocaleDateString('ko-KR')}</td>
        </tr>
      )),
    [videos]
  );

  return (
    <div className="app">
      <header className="header">
        <h1>YouTube Shorts 조회수 순위</h1>
        <p>특정 채널의 숏츠 영상을 조회수 순으로 확인해보세요.</p>
      </header>

      <div className="form-row">
        <input
          type="text"
          placeholder="채널 ID를 입력하세요"
          value={channelId}
          onChange={(e) => setChannelId(e.target.value)}
          style={{ flex: 1, minWidth: 240 }}
        />
        <input
          type="number"
          min="1"
          max="50"
          value={limit}
          onChange={(e) => setLimit(Number(e.target.value))}
          style={{ width: 120 }}
        />
        <button onClick={handleFetch} disabled={loading}>
          {loading ? '조회 중...' : '조회'}
        </button>
      </div>
      <div className="status">
        {error && <span className="error">{error}</span>}
      </div>

      <div className="table-wrapper">
        {loading && <p>불러오는 중...</p>}
        {!loading && videos.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>순위</th>
                <th>썸네일</th>
                <th>제목</th>
                <th>채널</th>
                <th>조회수</th>
                <th>업로드 날짜</th>
              </tr>
            </thead>
            <tbody>{tableRows}</tbody>
          </table>
        )}
        {!loading && videos.length === 0 && !error && (
          <p className="empty-state">아직 데이터가 없습니다. 채널 ID를 입력하고 조회해보세요.</p>
        )}
      </div>
    </div>
  );
}

export default App;
