import { Button, Result } from 'antd';
import { useNavigate } from 'react-router-dom';

const ForbiddenPage = () => {
  const navigate = useNavigate();
  return (
    <Result
      status="403"
      title="403"
      subTitle="Tài khoản của bạn không có quyền truy cập chức năng này."
      extra={
        <Button type="primary" onClick={() => navigate('/dashboard')}>
          Về trang tổng quan
        </Button>
      }
    />
  );
};

export default ForbiddenPage;
