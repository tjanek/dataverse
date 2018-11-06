ALTER TABLE public.filemetadata ADD displayorder integer DEFAULT 0 NULL;
UPDATE public.filemetadata SET displayorder = 0;