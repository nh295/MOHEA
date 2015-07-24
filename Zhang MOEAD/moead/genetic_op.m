function ind = genetic_op(index, updateneighbour, domain)
%GENETICOP function implemented the DE operation to generate a new
%individual from a subproblems and its neighbours.

%   subproblems: is all the subproblems.
%   index: the index of the subproblem need to handle.
%   domain: the domain of the origional multiobjective problem.
%   ind: is an individual structure.
  global params parDim;

  parents = mateselection(index, updateneighbour, 2);
  newpoint = de_crossover([index parents], params.F, params.CR, domain);
  
  ind = get_structure('individual');
  ind.parameter = newpoint;

  ind = realmutate(ind, domain, 1/parDim);
  % ind = gaussian_mutate(ind, 1/parDim, domain);
  
  clear points selectpoints oldpoint randomarray deselect newpoint parentindex si;
end

function ind = realmutate(ind, domains, rate)
%REALMUTATE Summary of this function goes here
%   Detailed explanation goes here

  % double rnd, delta1, delta2, mut_pow, deltaq;
  % double y, yl, yu, val, xy;
  % double eta_m = id_mu;
  global rnduni parDim;

  eta_m=20;
  
  if (isstruct(ind))
      a = ind.parameter;
  else
      a = ind;
  end
  
  %[r rnduni] = crandom(rnduni);
  %id_rnd = ceil(r*parDim);
  
  for j = 1:parDim
      %[r rnduni] = crandom(rnduni);
      r = rand;
      
      if (r <= rate) 
        y = a(j);
        yl = domains(j,1);
        yu = domains(j,2);
        delta1 = (y - yl) / (yu - yl);
        delta2 = (yu - y) / (yu - yl);

        %[rnd rnduni] = crandom(rnduni);
        rnd = rand;
        mut_pow = 1.0 / (eta_m + 1.0);
        if (rnd <= 0.5) 
	      xy = 1.0 - delta1;
	      val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (xy^(eta_m + 1.0));
	      deltaq = (val^mut_pow) - 1.0;
        else 
	      xy = 1.0 - delta2;
	      val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (xy^ (eta_m + 1.0));
	      deltaq = 1.0 - (val^mut_pow);
        end
	  
        y = y + deltaq * (yu - yl);
        if (y < yl)
	      y = yl;
        end
        if (y > yu)
	      y = yu;
        end
        
        a(j) = y;        
      end
  end
  
  if isstruct(ind)
      ind.parameter = a;
  else
      ind = a;
  end
end

function ind = gaussian_mutate( ind, prob, domain)
%GAUSSIAN_MUTATE Summary of this function goes here
%   Detailed explanation goes here

  if isstruct(ind)
      x = ind.parameter;
  else
      x  = ind;
  end

    parDim = length(x);
    lowend  = domain(:,1);
    highend =domain(:,2);
    sigma = (highend-lowend)./20;
    
    newparam = min(max(normrnd(x, sigma), lowend), highend);
    C = rand(parDim, 1)<prob;
    x(C) = newparam(C);
    
  if isstruct(ind)
      ind.parameter = x;
  else
      ind = x;
  end
end

% select the candidate for evoluationary mating, i.e. finding the DE parent.
function select = mateselection(index, updateneighbour, size)
  global subproblems rnduni;
  
  select=[];
  if (updateneighbour)
      parentindex = subproblems(index).neighbour;
  else
      parentindex = 1:length(subproblems);
  end
  
  while(length(select)<size)
      %[r rnduni] = crandom(rnduni);
      r = rand;
      parent = ceil(length(parentindex)*r);
      indexsel = parentindex(parent);
      if ~any(ismember(parent, select))
          select = [select indexsel];
      end
  end
end

function ind = de_crossover(parents, F, CR, domain)
  global subproblems rnduni parDim;
  
  %  [r rnduni]=crandom(rnduni);
  r = rand;
  jrandom = ceil(r*parDim);
  
  %retrieve the individuals.
  points = [subproblems(parents).curpoint];
  selectpoints = [points.parameter];
  
  cross = selectpoints(:,1) + F.*(selectpoints(:,3)-selectpoints(:,2));
  ind = selectpoints(:,1);
  
  %DE operation.
  for i=1:parDim
      %[r rnduni]=crandom(rnduni);
      r = rand;
      if (r<CR || i==jrandom)
          ind(i) = cross(i);
      end
      
      %handle the boundary.
      lowbound = domain(i,1);
      upbound = domain(i,2);
      if (ind(i)<lowbound)
          %[r rnduni]=crandom(rnduni);
          r = rand;
          ind(i)=lowbound + r*(selectpoints(i,1)-lowbound);
      elseif (ind(i)>upbound)
          %[r rnduni]=crandom(rnduni);
          r = rand;
          ind(i)=upbound - r*(upbound - selectpoints(i,1));
      end
  end
end