-- Invoice Maker schema for Supabase (Postgres + REST).
-- Paste this into the Supabase SQL editor and run it once.
--
-- In Authentication -> Providers -> Email, turn OFF "Confirm email"
-- so register signs the user in immediately.

create table if not exists public.invoices (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null default auth.uid() references auth.users (id) on delete cascade,
    invoice_number text not null default '',
    client_name text not null default '',
    client_email text not null default '',
    client_address text not null default '',
    client_phone text not null default '',
    base_currency text not null default 'ZAR',
    display_currency text not null default 'ZAR',
    exchange_rate double precision not null default 1,
    status text not null default 'DRAFT',
    notes text not null default '',
    created_at timestamptz not null default now(),
    due_at timestamptz not null default now()
);

create table if not exists public.line_items (
    id uuid primary key default gen_random_uuid(),
    invoice_id uuid not null references public.invoices (id) on delete cascade,
    description text not null default '',
    quantity double precision not null default 1,
    unit_price double precision not null default 0,
    position integer not null default 0
);

create index if not exists invoices_owner_idx on public.invoices (owner_id);
create index if not exists line_items_invoice_idx on public.line_items (invoice_id);

alter table public.invoices enable row level security;
alter table public.line_items enable row level security;

drop policy if exists "Users manage their own invoices" on public.invoices;
create policy "Users manage their own invoices"
    on public.invoices
    for all
    to authenticated
    using (owner_id = auth.uid())
    with check (owner_id = auth.uid());

drop policy if exists "Users manage lines on their own invoices" on public.line_items;
create policy "Users manage lines on their own invoices"
    on public.line_items
    for all
    to authenticated
    using (
        exists (
            select 1
            from public.invoices
            where invoices.id = line_items.invoice_id
              and invoices.owner_id = auth.uid()
        )
    )
    with check (
        exists (
            select 1
            from public.invoices
            where invoices.id = line_items.invoice_id
              and invoices.owner_id = auth.uid()
        )
    );

grant select, insert, update, delete on public.invoices to authenticated;
grant select, insert, update, delete on public.line_items to authenticated;
