# Следующие шаги — Applied Mekanistics / NL

1. Выполнить клиентский QA с SEMI: отображение chemical stacks, количество в EMI, перенос в Processing Pattern, химические ячейки и portable cell GUI.
2. Проверить построенную ME-сеть: Interface, Import/Export/Storage Bus, фильтры и инвертор, заполненная сеть, выключенная энергия, много выходов P2P, выгрузка чанка и перезапуск.
3. Провести полный Processing Pattern цикл с химическим входом/выходом и тест QIO Storage Bus с сохранением drive через перезапуск.
4. Повторить registry/cell/pattern проверки с Mekanism Extras после его адаптации. Текущий smoke автоматически использует namespace Extras, если мод загружен.
5. Следующий мод NL-очереди — **Mekanism Extras** согласно приоритету пользователя. MoreMachine подтверждён: commit `48e51b90`, чистая ветка `rebase/26.1-nl`, экспортированный JAR SHA-256 `2d6b7bcd3b341cafc7603bfca021bc0cdd1b55d9b8204b6ebce268b9b8d5fa35`; существующий отчёт фиксирует BUILD/SERVER PASS.

Не заменять наш Mekanism на Version-Locked зависимости. Не выполнять push/merge. Сохранять модульные коммиты и раздельные логи/экспорты.
