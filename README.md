# Aplikasi manajemen keuangan pribadi

## Download aplikasi disini [AturDana.apk](https://github.com/farelyudapratama/aturdana/raw/main/app/release/app-release.apk) (Gunakan Android diatas nougat)

## Fitur
- Mencatat pendapatan dan pengeluaran
- Melihat riwayat transaksi
- Memfilter transaksi berdasarkan tanggal
- Menghitung total transaksi
- Membuat Anggaran
- Membuat Pengingat

## Cara clone dan menjalankan di Android Studio
1. Clone repository ini
2. Buat projek baru di Firebase
3. Aktifkan Realtime Database, Authentication, dan storage
4. Pada Authentication pilih sign-in method Email/Password ![image](https://github.com/user-attachments/assets/7bd00da3-a568-4e5a-b05e-c2f772849e56)
5. Pada Realtime Database atur rules nya menjadi seperti ini:
      ```json
      {
        "rules": {
          ".read": "auth != null",
          ".write": "auth != null",
          "users": {
            "$uid": {
              ".read": "auth != null && auth.uid == $uid",
              ".write": "auth != null && auth.uid == $uid",
              "categories":{
                "$category_id":{
                  ".read": "auth != null && auth.uid == $uid",
              		".write": "auth != null && auth.uid == $uid",
                }
              }
            }
          },
          "transactions": {
            "$transaction_id": {
              ".read": "auth != null && data.child('user_id').val() == auth.uid",
              ".write": "auth != null && newData.child('user_id').val() == auth.uid"
            }
          },
          "budgets": {
            "$budget_id": {
              ".read": "auth != null && data.child('user_id').val() == auth.uid",
              ".write": "auth != null && newData.child('user_id').val() == auth.uid"
            }
          },
          "reminders": {
            "$reminder_id": {
              ".read": "auth != null && data.child('user_id').val() == auth.uid",
              ".write": "auth != null && newData.child('user_id').val() == auth.uid"
            }
          }
        }
      }
      ```
6. Pada Storage atur rulesnya seperti ini:
      ```
      rules_version = '2';
      
      service firebase.storage {
        match /b/{bucket}/o {
          match /{allPaths=**} {
            allow read, write: if request.auth != null;
          }
        }
      }
      ```
7. Pada project setting firebase tambahkan aplikasi androidnya
 - ![image](https://github.com/user-attachments/assets/5632ee8d-8f62-45de-b5aa-fec4b6b2a850)
 - ![image](https://github.com/user-attachments/assets/468058ef-35c7-4df7-870e-b7aae4f7bc38)

8. Selanjutnya download google-services.json yang diberikan firebase dan taruh file tersebut di folder app ![image](https://github.com/user-attachments/assets/54e67ee4-e9de-450a-97d3-2c5ded729967)
9. Terakhir hubungkan ke perangkat android atau emulator android Dan Jalankan aplikasi dengan klik " Run 'app' " pada android studio

