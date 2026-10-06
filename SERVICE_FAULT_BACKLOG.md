# Service fault backlog deployment

The application now groups approved requests by asset and assigns individual reported faults to job defects. One defect can address repeated reports across several requests. Creating a job assigns work; recording a repaired outcome resolves it. Deferring work releases the reports for another job and retains the original job history.

## Manual database step

Run `src/main/resources/db/service_fault_backlog.sql` manually against the application PostgreSQL database **before deploying/restarting the updated application**. The code does not run this migration. No database migration or application startup was executed during development of this change.

1. Back up the database and pause service-request/job writes during deployment.
2. Open the SQL file in your database client and execute the complete script. It wraps schema changes, seed data, and backfill in one transaction. If your client reports an error, roll back that transaction and correct the error before proceeding.
3. Check the verification queries at the bottom: the catalogue initially contains 41 faults. Review the job/request asset mismatch report and any unknown fault labels. The script deliberately does not repair those existing links.
4. Deploy the backend and frontend together. Do not run the old backend against the new workflow: old request writes do not maintain the individual fault records.
5. Open Incoming Service Requests. Review legacy faults marked REVIEW before selecting them for new work.

The migration adds `fault_type`, `service_request_fault`, and `job_defect_source`, extends `defect`, and widens `service_request.maintenance_works` to text. It preserves existing request text and `job_card.service_request_id` links. New job/request links are derived through the fault records. Receive notes derive the same links through their job.

Already-linked legacy requests are marked REVIEW, because old job links do not identify which faults were addressed. Unknown or missing legacy fault descriptions also require review. For each one, check the request's existing job history and enter evidence to either return it to pending or confirm it was previously repaired. Confirmation does not invent a historical fault/job assignment. The review note and timestamp are stored on the report.

The migration is written to tolerate rerunning the same version: existing reports and review decisions are not overwritten. Rollback after new fault work has been saved requires a database-maintainer plan; do not drop these tables and discard the new repair history.

## Workshop flow

1. Select an asset in Incoming Service Requests.
2. Expand a fault and select all pending reports or specific reports. Assigned, resolved, and unreviewed legacy reports are not selectable.
3. Create a job and review the selected reports on its form. If requests span projects, choose the requesting/return project explicitly. Reports arriving after selection are not silently included.
4. On the job actions page, use Reported faults addressed by this job to mark repairs or defer work with a reason.
5. Resolve or defer all assigned reported work before finishing/delivering. Open sub-jobs must be finished before their parent. Unselected and deferred reports remain pending.

A reported fault cannot be assigned to two jobs concurrently. Asset and report locks plus a partial unique database index enforce this. Create uses one transaction for the job, grouped defects, and source links. A stable client job UUID prevents double submission from creating another job. A conflict requires refreshing/checking the existing job rather than blindly retrying.

Job descriptions discovered during repair can still be added manually. Report-linked defects cannot be edited/deleted through the old defect API. Use their outcome controls instead. Request asset, fault text, and approval become immutable after work/history is linked or a legacy review is recorded.

Job cancellation is represented at the fault level by deferring its outstanding reported work; this does not introduce a new job-card cancellation status. Existing job status and close/delivery controls remain in use. Resolved/deferred outcomes are final; a new recurrence should be a new service request.

## Validation

The new backend unit tests use a mocked JdbcTemplate and never start Spring or connect to a database:

```text
mvn -Dtest=FaultWorkflowServiceTest test
```

Do not run the existing `ErpApplicationTests` as a substitute: it starts the application context using the configured datasource. Frontend tests cover grouped selection, partial request coverage, stale/new reports, disabled assigned reports, and API retries.

After the manual migration, verify in the application: two requests for one asset with an overlapping fault; select only that fault; confirm one job defect and two request links; defer it; assign it to another job; resolve it; confirm other faults remain pending. Also try selecting the same report in two browser tabs: the second save should return a conflict and create no partial job.

Live SQL execution, browser-to-backend integration, and database concurrency checks remain deployment checks because no database changes were authorized for the implementation agent.
