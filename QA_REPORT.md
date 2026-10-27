# Applied Mekanistics — QA 26.1.2 / NL

Дата: 2026-10-26. Ветка: `port/26.1-nl`. Продолжение community-port `4451b4b` и незакоммиченных изменений OpenCode NL; порт не начинался заново.

## Среда и воспроизведение

- Java 25.0.4.1; Gradle 9.6.1; ModDevGradle 2.0.146; NeoForge 26.1.2.122.
- Mekanism 10.8.0 homebaked (core + Generators/Tools/Additions), AE2 26.1.12-SNAPSHOT, GuideME 26.1.14-beta, SEMI/EMI 1.1.24-semi151a6c88+26.1.2 из текущего стека.
- `GRADLE_USER_HOME=/root/home/agent-work/mekanism-port-queue/gradle-home bash gradlew build portSources runServer -PportSmoke --max-workers=2 --console=plain`
- Изолированный сервер: `/root/home/agent-work/mekanism-port-queue/build/applied-mekanistics/server`, loopback:25585.
- Логи: `/root/home/agent-work/mekanism-port-queue/applied-*.log`.
- `runServer -PportSmoke` проверяет свежий файл результата: одного штатного завершения сервера недостаточно для PASS.

## Исправления

1. Удалены пять импортов старого `mekanism.api.Action`. Химические операции используют `ResourceHandler<ChemicalResource>` и NeoForge TransactionContext; Actionable.SIMULATE откатывается, MODULATE фиксируется.
2. Отрицательные запросы не изменяют хранилища; long-запросы к int API ограничиваются Integer.MAX_VALUE. QIO сохраняет long на всём пути.
3. Import/Export Bus резервируют химическую сторону транзакцией и согласуют количество с ME перед фактическим переносом. Частичный приём не требует обратной операции на однонаправленном баке. Удалены ветки, намеренно уничтожавшие невозвращённый остаток. Расход операций импорта округляется вверх.
4. ME Interface использует AE2 GenericStackInvHandler с родным snapshot journal, фильтрами, ограничениями слотов и уведомлением только после commit. Адаптер Storage Bus откладывает уведомления и проверяет извлекаемость конкретного слота.
5. Предметные контейнеры используют ItemAccess для курсора/слота, включая замену предмета и уведомления инвентаря. Просмотр извлекаемого содержимого выполняется с откатом.
6. QIO использует massInsert/massExtract с транзакциями, сохраняет проверку направления, владельца/режима доступа и валидности частоты.
7. Локальное исправление распределения химического P2P: остаток делится по числу выходов, перенос ограничен исходным количеством. Общий AE2 не изменён.
8. EMI: `EmiStack.getAmount()` вместо ошибочного `amount()`; при преобразовании в EMI сохранено количество GenericStack, а не единичное количество ключа. Старые JEI-классы удалены согласно исходным изменениям NL.
9. Обычные и переносные ячейки явно отвергают radioactive chemicals независимо от настройки radiationEnabled.

## Проверки

| Проверка | Результат / область |
|---|---|
| Исходная компиляция | Воспроизведены 6 ошибок; `applied-compile-baseline.log` |
| Gradle build | PASS; тесты выполняются внутри отдельного dedicated server, Gradle `test` — NO-SOURCE |
| Dedicated server с исходниками мода | PASS; несколько запусков и штатных сохранений/перезагрузок мира |
| Реестр химикатов | 64 непустых chemical entries Mekanism: key codec и encode/decode Processing Pattern |
| Chemical Storage Cell | Симуляция без изменения; insert/extract; persist и повторное открытие копии ItemStack |
| Радиоактивность | Все 5 обычных + 5 переносных ячеек; simulation и modulation; без остатка; запрет сохраняется при radiationEnabled=false |
| Нативный химический бак | Симуляция/commit; отрицательные и большие запросы; вложенный rollback |
| Предметный бак | Симуляция/commit insert и extract; сохранение количества при preview |
| ME Interface adapter | Отмена без уведомления; commit с одним уведомлением; вложенный rollback |
| Import/Export Strategy | Настоящая AE-ячейка ↔ нативный Mekanism-бак; сумма ресурсов сохранена. Контекст сети и бесконечная энергия — тестовые реализации |
| Частичные переносы | Отказ сети, ограниченный приём, однонаправленный бак, частично заполненный транзакционный получатель |
| P2P | Инициализация native handler; алгоритм распределения для 1/2/3/6/17 выходов, 0/1/5/10/1000/Integer.MAX_VALUE, отказ части получателей |
| QIO | Реальная QIOFrequency + Supermassive drive; 2,147,483,689 предметов; simulation/commit/rollback; oversized extract; отрицательный insert |
| QIO security | Реальная проверка private owner/stranger, стороны, invalid frequency; тестовый dashboard предоставляет частоту |
| JAR inspection | EMI присутствует; JEI/PortSmoke и вложенные зависимости отсутствуют; manifest/лицензии включены |

## Границы подтверждения

- Не выполнен интерактивный клиентский QA: рендер ячеек/химикатов, EMI экран и перенос в GUI, реальный курсор/слоты игрока.
- P2P проверен на уровне обработчика/распределения, не как многоблочная сеть с каналами и энергетикой. ME Interface — родной generic inventory adapter, не построенная сеть.
- Processing Patterns проверены encode/decode, не полный автоматический производственный цикл.
- QIO сохранение drive через перезапуск и полноценная связка Storage Bus + Dashboard требуют отдельного сценария; длинные значения и native transactional drive проверены в процессе сервера.
- MEStorage остаётся Actionable API без общего механизма rollback. Мост опирается на контракт корректной симуляции/выполнения ME и детерминированный replay NeoForge handler. Произвольные сторонние реализации, нарушающие этот контракт или бросающие исключения после необратимого изменения, не покрываются гарантией атомарности. Невалидный результат/replay вызывает явную ошибку вместо тихого void.
- Исходный smoke тест включал пустую запись химического реестра; исправлен. Первый QIO long-тест использовал слишком маленький Time-Dilating drive; заменён на Supermassive. Эти неуспешные прогоны сохранены в логах и не засчитаны как PASS.

Общий Mekanism, AE2 и репозитории других агентов не изменялись. Push/merge не выполнялись.

## Финальный функциональный прогон

`applied-server-smoke-final.log`: **1051 PASS**, `APPMEK FUNCTIONAL COMPLETE`, `BUILD SUCCESSFUL`; 64 непустые записи реестра химикатов. После остановки все измерения сохранены штатно.

Артефакты:

- `applied-mekanistics-26.1.2-1.6.0-port1-nl.jar`, SHA-256 `32931ddf366091715fa67f76ae46b78dd03b32382b052b1dbb0b97c09a0e8c1c`.
- `applied-mekanistics-26.1.2-1.6.0-port1-nl-sources.jar`, SHA-256 `75a93226de6f6ed7e3980ee0cbbc42dc2626bc7cd28f2f6f8333bcdc4cc3dafb`.

## Проверка выпускаемого JAR

PASS: `applied-jar-server.log`; загружен `appmek (jar(mods/applied-mekanistics-26.1.2-1.6.0-port1-nl.jar))`, каталоги dev-классов исключены из classpath. Сервер дошёл до Done, выполнил `list`, `datapack list enabled`, `save-all flush`, `stop`, завершился с кодом 0. ERROR/FATAL отсутствуют. Финализация артефактов: 2026-10-27.
