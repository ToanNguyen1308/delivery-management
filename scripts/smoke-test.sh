#!/usr/bin/env bash
# ==========================================================
# Kich ban kiem thu nhanh toan bo quy trinh nghiep vu qua REST API.
# Cach dung: bash scripts/smoke-test.sh [BASE_URL]
# Mac dinh BASE_URL = http://localhost:8080/api/v1
# ==========================================================
set -uo pipefail

BASE="${1:-http://localhost:8080/api/v1}"

j() { python3 -c "import sys,json;d=json.load(sys.stdin);exec(sys.argv[1])" "$1"; }
login() {
  curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}"
}
token() { login "$1" "$2" | j "print(d['data']['accessToken'])"; }

echo "===== 1. DANG NHAP 4 VAI TRO ====="
ADMIN_T=$(token admin 'Admin@123')
DISP_T=$(token dispatcher 'Dispatch@123')
CUS_T=$(token customer01 'Customer@123')
for u in admin dispatcher shipper01 customer01; do
  case $u in
    admin) p='Admin@123';; dispatcher) p='Dispatch@123';;
    shipper01) p='Shipper@123';; *) p='Customer@123';;
  esac
  login "$u" "$p" | j "print('  ',d['data']['user']['username'],'| nhom quyen:',d['data']['roleGroups'],'| so chuc nang:',len(d['data']['permissions']))"
done

echo "===== 2. SAI MAT KHAU -> THONG BAO LOI DA DUOC DICH ====="
curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"sai-mat-khau"}' | j "print('  ',d['code'],'|',d['message'])"

echo "===== 3. TINH THU CUOC VAN CHUYEN (EXPRESS + voucher GIAM20) ====="
curl -s -X POST "$BASE/pricing/preview" -H "Authorization: Bearer $CUS_T" -H 'Content-Type: application/json' -d '{
  "serviceType":"EXPRESS","weightKg":5,
  "pickupLatitude":21.0285,"pickupLongitude":105.8542,
  "deliveryLatitude":21.0136,"deliveryLongitude":105.8290,
  "codAmount":500000,"voucherCode":"GIAM20"}' \
  | j "x=d['data'];print('   quang duong',x['distanceKm'],'km | co ban',x['baseFee'],'| quang duong',x['distanceFee'],'| khoi luong',x['weightFee'],'| phu phi',x['surcharge'],'| phi COD',x['codFee'],'=> cuoc',x['shippingFee'],'- giam',x['discountAmount'],'= tra',x['totalAmount'])"

echo "===== 4. KHACH HANG TAO DON (COD 500k, voucher FREESHIP) ====="
ORDER=$(curl -s -X POST "$BASE/orders" -H "Authorization: Bearer $CUS_T" -H 'Content-Type: application/json' -d '{
  "senderName":"Nguyen Duc Toan","senderPhone":"0922000001",
  "pickupAddress":"144 Xuan Thuy, Cau Giay, Ha Noi","pickupDistrict":"Cau Giay","pickupProvince":"Ha Noi",
  "pickupLatitude":21.0313,"pickupLongitude":105.7967,
  "receiverName":"Tran Thi Bich","receiverPhone":"0987654321",
  "deliveryAddress":"25 Ly Thuong Kiet, Hoan Kiem, Ha Noi","deliveryDistrict":"Hoan Kiem","deliveryProvince":"Ha Noi",
  "deliveryLatitude":21.0245,"deliveryLongitude":105.8412,
  "packageDescription":"Quan ao","weightKg":2.5,"serviceType":"STANDARD","paymentMethod":"COD",
  "codAmount":500000,"voucherCode":"FREESHIP","note":"Giao gio hanh chinh",
  "items":[{"itemName":"Ao so mi","quantity":2,"unitPrice":250000,"weightKg":1.25}]}')
echo "$ORDER" | j "x=d['data'];print('   ma van don',x['orderCode'],'| trang thai',x['status']['description'],'| cuoc',x['shippingFee'],'- giam',x['discountAmount'],'= tra',x['totalAmount'],'| buoc tiep theo',[s['code'] for s in x['nextStatuses']])"
OID=$(echo "$ORDER" | j "print(d['data']['id'])")
OCODE=$(echo "$ORDER" | j "print(d['data']['orderCode'])")

echo "===== 5. CHUYEN TRANG THAI SAI LUONG PHAI BI CHAN ====="
curl -s -X PUT "$BASE/orders/$OID/status" -H "Authorization: Bearer $DISP_T" -H 'Content-Type: application/json' \
  -d '{"status":"DELIVERED"}' | j "print('  ',d['code'],'|',d['message'])"

echo "===== 6. DIEU PHOI VIEN XAC NHAN DON ====="
curl -s -X PUT "$BASE/orders/$OID/confirm" -H "Authorization: Bearer $DISP_T" \
  | j "print('   trang thai:',d['data']['status']['description'])"

echo "===== 7. GAN SHIPPER TU DONG (chien luoc NEAREST) ====="
ASG=$(curl -s -X POST "$BASE/dispatch/assign" -H "Authorization: Bearer $DISP_T" -H 'Content-Type: application/json' \
  -d "{\"orderId\":$OID}")
echo "$ASG" | j "x=d['data'];print('   chon',x['shipperCode'],x['shipperName'],'| kieu gan:',x['assignType']['description'],'| cach diem lay',x['distanceKm'],'km')"
AID=$(echo "$ASG" | j "print(d['data']['id'])")
SHIPPER_CODE=$(echo "$ASG" | j "print(d['data']['shipperCode'])")
SHIPPER_USER=$(echo "$SHIPPER_CODE" | sed 's/SP0000/shipper0/')
SHIP_T=$(token "$SHIPPER_USER" 'Shipper@123')
echo "   dang nhap bang tai khoan shipper duoc gan: $SHIPPER_USER"

echo "===== 8. SHIPPER NHAN DON ====="
curl -s -X PUT "$BASE/dispatch/assignments/$AID/respond" -H "Authorization: Bearer $SHIP_T" \
  -H 'Content-Type: application/json' -d '{"accepted":true}' \
  | j "print('   phan cong:',d['data']['status']['description'])"

echo "===== 9. SHIPPER GUI VI TRI GPS (day realtime qua WebSocket) ====="
for point in "21.0300,105.8000" "21.0285,105.8150" "21.0260,105.8300"; do
  lat="${point%,*}"; lng="${point#*,}"
  curl -s -X POST "$BASE/tracking/location" -H "Authorization: Bearer $SHIP_T" -H 'Content-Type: application/json' \
    -d "{\"latitude\":$lat,\"longitude\":$lng,\"orderId\":$OID,\"speedKmh\":28.5}" \
    | j "print('   vi tri moi:',d['data']['latitude'],d['data']['longitude'])"
done

echo "===== 10. SHIPPER CAP NHAT: LAY HANG -> DANG VAN CHUYEN ====="
curl -s -X PUT "$BASE/orders/$OID/status" -H "Authorization: Bearer $SHIP_T" -H 'Content-Type: application/json' \
  -d '{"status":"PICKED_UP","note":"Da lay hang tai diem gui","latitude":21.0313,"longitude":105.7967}' \
  | j "print('   ->',d['data']['status']['description'])"
curl -s -X PUT "$BASE/orders/$OID/status" -H "Authorization: Bearer $SHIP_T" -H 'Content-Type: application/json' \
  -d '{"status":"IN_TRANSIT","note":"Dang tren duong giao","latitude":21.0280,"longitude":105.8200}' \
  | j "print('   ->',d['data']['status']['description'])"

echo "===== 11. GIAO THANH CONG MA THIEU ANH XAC NHAN -> BI CHAN ====="
curl -s -X PUT "$BASE/orders/$OID/status" -H "Authorization: Bearer $SHIP_T" -H 'Content-Type: application/json' \
  -d '{"status":"DELIVERED"}' | j "print('  ',d['code'],'|',d['message'])"

echo "===== 12. GIAO THANH CONG KEM ANH XAC NHAN ====="
curl -s -X PUT "$BASE/orders/$OID/status" -H "Authorization: Bearer $SHIP_T" -H 'Content-Type: application/json' -d '{
  "status":"DELIVERED","note":"Nguoi nhan da ky nhan",
  "proofImageUrl":"http://localhost:9000/delivery-files/delivery-proof/2026/09/pod.jpg",
  "latitude":21.0245,"longitude":105.8412}' \
  | j "x=d['data'];print('   ->',x['status']['description'],'| doi soat COD:',x['codSettlementStatus']['description'])"

echo "===== 13. VI COD CUA SHIPPER ====="
curl -s "$BASE/cod-settlements/my-wallet" -H "Authorization: Bearer $SHIP_T" \
  | j "x=d['data'];print('   dang giu',x['holdingAmount'],'VND /',x['holdingCount'],'don')"

echo "===== 14. SHIPPER NOP TIEN -> KE TOAN XAC NHAN ====="
curl -s -X POST "$BASE/cod-settlements/submit-all" -H "Authorization: Bearer $SHIP_T" \
  | j "print('   da nop',d['data'],'khoan')"
SID=$(curl -s -X POST "$BASE/cod-settlements/search" -H "Authorization: Bearer $DISP_T" -H 'Content-Type: application/json' \
  -d '{"statuses":["SUBMITTED"]}' | j "print(d['data']['content'][0]['id'])")
curl -s -X PUT "$BASE/cod-settlements/$SID/confirm?note=Da%20nhan%20du%20tien" -H "Authorization: Bearer $DISP_T" \
  | j "print('   doi soat:',d['data']['status']['description'])"

echo "===== 15. TIMELINE HANH TRINH ====="
curl -s "$BASE/tracking/orders/$OID" -H "Authorization: Bearer $CUS_T" | j "
x=d['data']
print('   trang thai',x['status']['description'],'| shipper',x['shipperName'],'| so diem GPS',len(x['route']))
[print('    -',e['occurredAt'][11:19],e['eventType']['description'],'|',e['description']) for e in x['events']]"

echo "===== 16. TRA CUU CONG KHAI (khong can dang nhap, du lieu duoc che) ====="
curl -s "$BASE/public/tracking/$OCODE" | j "
x=d['data']
print('   nguoi nhan:',x['receiverName'],'| dia chi:',x['deliveryAddress'])
print('   so dien thoai shipper duoc an:', 'shipperPhone' not in x or x.get('shipperPhone') is None)
print('   lo trinh GPS duoc an:',len(x['route'])==0)"

echo "===== 17. PHAN QUYEN DU LIEU ====="
C2_T=$(token customer02 'Customer@123')
curl -s "$BASE/orders/$OID" -H "Authorization: Bearer $C2_T" \
  | j "print('   customer02 xem don cua customer01 ->',d['code'],'|',d['message'])"
curl -s -X POST "$BASE/users/search" -H "Authorization: Bearer $C2_T" -H 'Content-Type: application/json' -d '{}' \
  | j "print('   customer02 goi API quan tri  ->',d['code'],'|',d['message'])"
curl -s -X POST "$BASE/orders/search" -H "Authorization: Bearer $C2_T" -H 'Content-Type: application/json' -d '{}' \
  | j "print('   customer02 xem danh sach don ->',d['data']['totalElements'],'don (chi cua chinh minh)')"

echo "===== 18. LICH SU CHUYEN TRANG THAI ====="
curl -s "$BASE/orders/$OID/history" -H "Authorization: Bearer $ADMIN_T" \
  | j "[print('    ',(h['fromStatus']['code'] if h.get('fromStatus') else 'MOI'),'->',h['toStatus']['code'],'|',h['changedByName']) for h in d['data']]"

echo "===== 19. DASHBOARD & THONG KE ====="
curl -s -X POST "$BASE/dashboard/overview" -H "Authorization: Bearer $ADMIN_T" -H 'Content-Type: application/json' -d '{}' \
  | j "x=d['data'];print('   tong don',x['totalOrders'],'| giao thanh cong',x['deliveredOrders'],'| ti le',x['successRate'],'% | doanh thu',x['totalRevenue'],'| COD thu ho',x['totalCodCollected'],'| shipper truc tuyen',x['onlineShippers'])"
curl -s -X POST "$BASE/dashboard/orders-by-status" -H "Authorization: Bearer $ADMIN_T" -H 'Content-Type: application/json' -d '{}' \
  | j "[print('    ',x['status']['description'],'=',x['quantity']) for x in d['data']]"
curl -s -X POST "$BASE/dashboard/top-shippers" -H "Authorization: Bearer $ADMIN_T" -H 'Content-Type: application/json' -d '{}' \
  | j "[print('    ',x['shipperCode'],x['fullName'],'-',x['deliveredCount'],'don thanh cong') for x in d['data']]"

echo "===== 20. THONG BAO TRONG UNG DUNG ====="
curl -s "$BASE/notifications/unread-count" -H "Authorization: Bearer $CUS_T" | j "print('   chua doc:',d['data'])"
curl -s -X POST "$BASE/notifications/search" -H "Authorization: Bearer $CUS_T" -H 'Content-Type: application/json' -d '{"size":5}' \
  | j "[print('    -',n['title'],'|',n['content']) for n in d['data']['content']]"

echo "===== 21. EXPORT EXCEL ====="
curl -s -X POST "$BASE/orders/export" -H "Authorization: Bearer $ADMIN_T" -H 'Content-Type: application/json' -d '{}' \
  -o /tmp/don-hang.xlsx -w "   file xuat ra: %{size_download} bytes, HTTP %{http_code}\n"
curl -s "$BASE/orders/import-template" -H "Authorization: Bearer $ADMIN_T" \
  -o /tmp/mau-import.xlsx -w "   file mau import: %{size_download} bytes, HTTP %{http_code}\n"

echo "===== HOAN TAT ====="
