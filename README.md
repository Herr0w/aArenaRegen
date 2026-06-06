# aArenaRegen

aArenaRegen, Minecraft Paper 1.21.4 için yüksek performanslı arena yenileme eklentisidir. Seçilen arena bölgelerini kaydeder ve belirlenen aralıklarla lag oluşturmadan, parça parça yeniler.

## Özellikler

- Büyük arenalar için optimize edilmiş batch sistemi
- Sadece değişen blokları yeniler
- Arenaya sonradan koyulan ekstra blokları temizler
- Sıkıştırılmış binary arena dosyaları kullanır
- Otomatik ve manuel arena yenileme desteği
- Ayarlanabilir performans değerleri

## Komutlar

- /arenargen wand - Seçim çubuğu verir
- /arenargen save <arena> - Seçili bölgeyi kaydeder
- /arenargen regen <arena> - Arenayı manuel yeniler
- /arenargen list - Arenaların listesini gösterir
- /arenargen info <arena> - Arena bilgilerini gösterir
- /arenargen delete <arena> - Arenayı siler
- /arenargen reload - Ayarları yeniler

## İzinler

- arenargen.admin
- arenargen.wand
- arenargen.save
- arenargen.regen
- arenargen.info
- arenargen.delete
- arenargen.reload

## Kurulum

ArenaRegen-1.0.jar dosyasını plugins klasörüne atın ve sunucuyu yeniden başlatın. Ayarlar plugins/ArenaRegen/config.yml dosyasından düzenlenebilir.
